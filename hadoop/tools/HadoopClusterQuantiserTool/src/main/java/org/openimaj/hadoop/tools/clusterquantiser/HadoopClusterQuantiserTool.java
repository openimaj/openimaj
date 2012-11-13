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
package org.openimaj.hadoop.tools.clusterquantiser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.kohsuke.args4j.CmdLineException;
import org.openimaj.hadoop.mapreduce.TextBytesJobUtil;
import org.openimaj.hadoop.sequencefile.MetadataConfiguration;
import org.openimaj.hadoop.sequencefile.TextBytesSequenceFileUtility;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.IntCentroidsResult;
import org.openimaj.ml.clustering.SpatialClusters;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.assignment.hard.KDTreeByteEuclideanAssigner;
import org.openimaj.ml.clustering.assignment.hard.KDTreeIntEuclideanAssigner;
import org.openimaj.tools.clusterquantiser.ClusterQuantiser;
import org.openimaj.tools.clusterquantiser.FeatureFile;
import org.openimaj.tools.clusterquantiser.FeatureFileFeature;
import org.openimaj.util.array.ByteArrayConverter;

public class HadoopClusterQuantiserTool extends Configured implements Tool {
	private static final String ARGS_KEY = "clusterquantiser.args";

	static class ClusterQuantiserMapper extends Mapper<Text, BytesWritable, Text, BytesWritable> {
		private static SpatialClusters<?> tree = null;
		private static HardAssigner<?, ?, ?> assigner = null;
		private static HadoopClusterQuantiserOptions options = null;

		protected static synchronized void loadCluster(Mapper<Text, BytesWritable, Text, BytesWritable>.Context context)
				throws IOException
		{
			if (options == null) {
				try {
					options = new HadoopClusterQuantiserOptions(context.getConfiguration().getStrings(ARGS_KEY));
					options.prepare();
				} catch (final CmdLineException e) {
					throw new IOException(e);
				}
			}

			if (tree == null) {
				InputStream ios = null;
				try {
					System.out.print("Reading quant data. ");
					ios = options.getClusterInputStream();
					tree = IOUtils.read(ios, options.getClusterClass());

					if (tree instanceof ByteCentroidsResult)
						assigner = new KDTreeByteEuclideanAssigner((ByteCentroidsResult) tree);
					else if (tree instanceof IntCentroidsResult)
						assigner = new KDTreeIntEuclideanAssigner((IntCentroidsResult) tree);
					else
						assigner = tree.defaultHardAssigner();

					System.out.println("Done reading quant data.");
				} catch (final IOException e) {
					e.printStackTrace();
					throw e;
				} finally {
					if (ios != null)
						ios.close();
				}
			} else {
				System.out.println("tree already loaded");
			}
		}

		@Override
		protected void setup(Mapper<Text, BytesWritable, Text, BytesWritable>.Context context) throws IOException,
				InterruptedException
		{
			loadCluster(context);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void
				map(Text key, BytesWritable value, Mapper<Text, BytesWritable, Text, BytesWritable>.Context context)
						throws java.io.IOException, InterruptedException
		{
			try {
				final long t1 = System.currentTimeMillis();

				System.out.println("[" + Thread.currentThread().getId() + "]" + "Calling map ");
				ByteArrayOutputStream baos = null;
				if (options.isInfoMode()) {
					ClusterQuantiser.do_info(options);
				} else if (options.isQuantMode()) {

					final FeatureFile input = options.getFileType().read(new ByteArrayInputStream(value.getBytes()));

					baos = new ByteArrayOutputStream();
					PrintWriter pw = null;
					try {
						pw = new PrintWriter(baos);
						pw.format("%d\n%d\n", input.size(), tree.numClusters());

						for (final FeatureFileFeature fff : input) {
							int cluster = -1;

							if (tree.getClass().getName().contains("Byte"))
								cluster = ((HardAssigner<byte[], ?, ?>) assigner).assign(fff.data);
							else
								cluster = ((HardAssigner<int[], ?, ?>) assigner).assign(ByteArrayConverter
										.byteToInt(fff.data));

							pw.format("%s %d\n", fff.location.trim(), cluster);
						}
					} finally {
						if (pw != null) {
							pw.flush();
							pw.close();
							input.close();
						}
					}

					context.write(key, new BytesWritable(baos.toByteArray()));
				}
				final long t2 = System.currentTimeMillis();
				System.out.println("[" + Thread.currentThread().getId() + "]" + "Job time taken: " + (t2 - t1) / 1000.0
						+ "s");
			} catch (final Exception e) {
				System.err.println("Failed to quantise features because: " + e.getMessage());
			}
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		final HadoopClusterQuantiserOptions options = new HadoopClusterQuantiserOptions(args, true);
		options.prepare();
		// String clusterFileString = options.getInputFileString();
		final Path[] paths = options.getInputPaths();
		// DistributedCache.addCacheFile(SequenceFileUtility.convertToURI(clusterFileString),
		// this.getConf());
		final TextBytesSequenceFileUtility util = new TextBytesSequenceFileUtility(paths[0].toUri(), true);
		final Map<String, String> metadata = new HashMap<String, String>();
		if (util.getUUID() != null)
			metadata.put(MetadataConfiguration.UUID_KEY, util.getUUID());
		metadata.put(MetadataConfiguration.CONTENT_TYPE_KEY, "application/quantised-"
				+ options.getClusterType().toString().toLowerCase() + "-" + options.getExtension());

		metadata.put("clusterquantiser.clustertype", options.getClusterType().toString());
		metadata.put("clusterquantiser.filetype", options.getFileType().toString());
		metadata.put("clusterquantiser.countmode", "" + options.getCountMode());
		metadata.put("clusterquantiser.extention", "" + options.getExtension());

		final Job job = TextBytesJobUtil.createJob(options.getInputFileString(), options.getOutputFileString(), metadata,
				this.getConf());
		job.setJarByClass(this.getClass());
		// job.setMapperClass(MultithreadedMapper.class);
		// MultithreadedMapper.setNumberOfThreads(job,
		// options.getConcurrency());
		// MultithreadedMapper.setMapperClass(job,
		// ClusterQuantiserMapper.class);
		// System.out.println("NThreads = " +
		// MultithreadedMapper.getNumberOfThreads(job));
		options.mapperModeOp.prepareJobMapper(job, ClusterQuantiserMapper.class, options);

		job.getConfiguration().setStrings(ARGS_KEY, args);
		job.setNumReduceTasks(0);
		((JobConf) job.getConfiguration()).setNumTasksToExecutePerJvm(-1);
		// job.getConfiguration().set("mapred.child.java.opts", "-Xmx3000M");
		job.waitForCompletion(true);
		return 0;
	}

	public static void main(String[] args) throws Exception {
		try {
			ToolRunner.run(new HadoopClusterQuantiserTool(), args);
		} catch (final CmdLineException e) {
			System.err.print(e);
		}
	}
}
