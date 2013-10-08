/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.hadoop.tools.image.indexing;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.hadoop.mapreduce.TextBytesJobUtil;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.lsh.functions.DoubleGaussianFactory;
import org.openimaj.lsh.sketch.IntLSHSketcher;
import org.openimaj.util.hash.HashFunction;
import org.openimaj.util.hash.HashFunctionFactory;
import org.openimaj.util.hash.modifier.LSBModifier;

import cern.jet.random.engine.MersenneTwister;

/**
 * Tool to convert SIFT features to LSH sketch form.
 * 
 * Mapper is <key, [sift_features]> -> <(index,hash), key>
 * <p>
 * Reducer output is <hash, [keys]> for each index
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class HadoopSiftLSHExtractor extends Configured implements Tool {
	public static class MapperOut implements WritableComparable<MapperOut> {
		public byte index;
		public int hash;

		@Override
		public void write(DataOutput out) throws IOException {
			out.writeByte(index);
			out.writeInt(hash);
		}

		@Override
		public void readFields(DataInput in) throws IOException {
			index = in.readByte();
			hash = in.readInt();
		}

		@Override
		public int compareTo(MapperOut o) {
			final int thisVal = this.hash;
			final int anotherVal = o.hash;
			return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
		}
	}

	public static class Sketcher {
		private static final int nbits = 128;
		private static final int ndims = 128;
		private static final int seed = 1;
		private static final double w = 6.0;
		final float LOG_BASE = 0.001f;
		private IntLSHSketcher<double[]> sketcher;

		public Sketcher() {
			final MersenneTwister rng = new MersenneTwister(seed);

			final DoubleGaussianFactory gauss = new DoubleGaussianFactory(ndims, rng, w);
			final HashFunctionFactory<double[]> factory = new HashFunctionFactory<double[]>() {
				@Override
				public HashFunction<double[]> create() {
					return new LSBModifier<double[]>(gauss.create());
				}
			};

			sketcher = new IntLSHSketcher<double[]>(factory, nbits);
		}

		public int[] sketch(Keypoint k) {
			return sketcher.createSketch(logScale(k.ivec, LOG_BASE));
		}

		double[] logScale(byte[] v, float l) {
			final double[] dfv = new double[v.length];
			final double s = -Math.log(l);

			for (int i = 0; i < v.length; i++) {
				double d = (v[i] + 128.0) / 256.0;

				if (d < l)
					d = l;
				d = (Math.log(d) + s) / s;
				if (d > 1.0)
					d = 1.0;

				dfv[i] = d;
			}
			return dfv;
		}
	}

	public static class LSHMapper extends Mapper<Text, BytesWritable, MapperOut, Text> {
		Sketcher sketcher = new Sketcher();

		private void process(List<Keypoint> features, Text key, Context context) throws IOException, InterruptedException
		{
			for (final Keypoint k : features) {
				final int[] sketch = sketcher.sketch(k);

				for (byte i = 0; i < sketch.length; i++) {
					final MapperOut mo = new MapperOut();
					mo.index = i;
					mo.hash = sketch[i];

					context.write(mo, key);
				}
			}
		}

		@Override
		public void map(Text key, BytesWritable data, Context context) throws IOException, InterruptedException {
			final List<Keypoint> features = MemoryLocalFeatureList.read(
					new ByteArrayInputStream(data.getBytes()), Keypoint.class);

			process(features, key, context);
		}
	}

	/**
	 * Partitioner that sends the data to a reducer based on the
	 * {@link MapperOut#index}.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static class LSHPartitioner extends Partitioner<MapperOut, Text> {
		@Override
		public int getPartition(MapperOut key, Text value, int numPartitions) {
			return key.index;
		}
	}

	/**
	 * Comparator to group the maps outputs by their {@link MapperOut#hash}.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public static class LSHGroupingComparator extends WritableComparator {
		public LSHGroupingComparator() {
			super(MapperOut.class, true);
		}

		@SuppressWarnings("rawtypes")
		@Override
		public int compare(WritableComparable a, WritableComparable b) {
			final int aVal = ((MapperOut) a).hash;
			final int bVal = ((MapperOut) b).hash;
			return (aVal < bVal ? -1 : (aVal == bVal ? 0 : 1));
		}
	}

	public static class LSHReducer extends Reducer<MapperOut, Text, IntWritable, Text> {
		@Override
		protected void reduce(MapperOut key, Iterable<Text> values, Context context) throws IOException,
				InterruptedException
		{
			// System.out.println("Task: " + context.getTaskAttemptID() +
			// " -> index: " + key.index);

			final Set<String> valSet = new HashSet<String>();
			for (final Text t : values) {
				valSet.add(t.toString());
			}

			final List<String> list = new ArrayList<String>(valSet);
			Collections.sort(list); // order naturally

			String s = list.get(0);
			for (int i = 1; i < list.size(); i++)
				s += " " + list.get(i);

			context.write(new IntWritable(key.hash), new Text(s));
		}
	}

	@Option(
			name = "--dont-compress-output",
			required = false,
			usage = "Don't compress sequencefile records.",
			metaVar = "BOOLEAN")
	private boolean dontcompress = false;

	@Option(
			name = "--remove",
			aliases = "-rm",
			required = false,
			usage = "Remove the existing output location if it exists.",
			metaVar = "BOOLEAN")
	private boolean replace = false;

	@Option(name = "--input", aliases = "-i", required = true, usage = "Input local features file.", metaVar = "STRING")
	private String input;

	@Option(name = "--output", aliases = "-o", required = true, usage = "Output pca-vlad file.", metaVar = "STRING")
	private String output;

	@Override
	public int run(String[] args) throws Exception {
		final CmdLineParser parser = new CmdLineParser(this);

		try {
			parser.parseArgument(args);
		} catch (final CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: hadoop jar HadoopImageIndexer.jar [options]");
			parser.printUsage(System.err);
			return -1;
		}

		final Path[] paths = SequenceFileUtility.getFilePaths(input, "part");
		final Path outputPath = new Path(output);

		if (outputPath.getFileSystem(this.getConf()).exists(outputPath) && replace)
			outputPath.getFileSystem(this.getConf()).delete(outputPath, true);

		final Job job = TextBytesJobUtil.createJob(paths, outputPath, null, this.getConf());

		job.setMapOutputKeyClass(MapperOut.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);

		job.setJarByClass(this.getClass());

		job.setMapperClass(LSHMapper.class);
		job.setReducerClass(LSHReducer.class);

		job.setNumReduceTasks(4);
		job.setPartitionerClass(LSHPartitioner.class);
		job.setGroupingComparatorClass(LSHGroupingComparator.class);

		SequenceFileOutputFormat.setCompressOutput(job, !dontcompress);
		job.waitForCompletion(true);

		return 0;
	}

	/**
	 * Main method
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ToolRunner.run(new HadoopSiftLSHExtractor(), args);
	}
}
