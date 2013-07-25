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
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
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
import org.openimaj.image.indexing.vlad.VLADIndexerData;

/**
 * Indexer for Product-quantised VLAD-PCA features. Consumes existing
 * local-features and requires a {@link VLADIndexerData} to provide the data.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class HadoopPqPcaVladIndexer extends Configured implements Tool {
	/**
	 * {@link Mapper} for extracting PQ-PCA-VLAD features from sets of local
	 * features. Also outputs the raw PCA-VLAD features.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	static class PqPcaVladMapper extends Mapper<Text, BytesWritable, Text, BytesWritable> {
		static enum COUNTERS {
			EMIT, NULL;
		}

		private VLADIndexerData indexer;
		private MultipleOutputs<Text, BytesWritable> mos;

		@Override
		protected void setup(Context context) throws IOException, InterruptedException
		{
			indexer = VLADIndexerData.read(new File("vlad-data.bin"));
			mos = new MultipleOutputs<Text, BytesWritable>(context);
		}

		@Override
		protected void map(Text key, BytesWritable value, Context context)
				throws IOException, InterruptedException
		{
			final List<Keypoint> keys = MemoryLocalFeatureList.read(new ByteArrayInputStream(value.getBytes()),
					Keypoint.class);

			final float[] vladData = indexer.extractPcaVlad(keys);

			if (vladData == null) {
				context.getCounter(COUNTERS.NULL).increment(1L);
				System.out.println("VLAD is null; keys has length " + keys.size());
				return;
			}

			final byte[] pqVladData = indexer.getProductQuantiser().quantise(vladData);

			mos.write("pcavlad", key, floatToBytes(vladData));

			context.write(key, new BytesWritable(pqVladData));
			context.getCounter(COUNTERS.EMIT).increment(1L);
		}

		BytesWritable floatToBytes(float[] arr) throws IOException {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final DataOutputStream dos = new DataOutputStream(baos);
			for (final float f : arr)
				dos.writeFloat(f);

			return new BytesWritable(baos.toByteArray());
		}

		@Override
		protected void cleanup(Context context) throws IOException,
				InterruptedException
		{
			super.cleanup(context);
			mos.close();
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

	@Option(name = "--indexer-data", aliases = "-id", required = true, usage = "Indexer data file.", metaVar = "STRING")
	private String indexerData;

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
		job.setJarByClass(this.getClass());
		job.setMapperClass(PqPcaVladMapper.class);
		job.setNumReduceTasks(0);

		MultipleOutputs.addNamedOutput(job, "pcavlad", SequenceFileOutputFormat.class, Text.class, BytesWritable.class);

		DistributedCache.createSymlink(job.getConfiguration());
		DistributedCache.addCacheFile(new URI(indexerData + "#vlad-data.bin"), job.getConfiguration());

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
		ToolRunner.run(new HadoopPqPcaVladIndexer(), args);
	}
}
