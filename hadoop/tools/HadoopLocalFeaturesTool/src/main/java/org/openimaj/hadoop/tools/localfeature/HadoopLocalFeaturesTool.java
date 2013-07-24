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
package org.openimaj.hadoop.tools.localfeature;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.hadoop.mapreduce.TextBytesJobUtil;
import org.openimaj.hadoop.sequencefile.MetadataConfiguration;
import org.openimaj.hadoop.sequencefile.TextBytesSequenceFileUtility;
import org.openimaj.io.IOUtils;
import org.openimaj.time.Timer;

/**
 * Hadoop version of the LocalFeaturesTool. Capable of extracting features from
 * images in sequencefiles.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class HadoopLocalFeaturesTool extends Configured implements Tool {
	private static final String ARGS_KEY = "openimaj.localfeaturestool.args";

	/**
	 * Feature extraction mapper.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 */
	static class LocalFeaturesMapper extends Mapper<Text, BytesWritable, Text, BytesWritable> {
		static enum Counters {
			SUCCESSFUL, FAILED;
		}

		private static final Logger logger = Logger.getLogger(LocalFeaturesMapper.class);
		private HadoopLocalFeaturesToolOptions options;

		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException
		{
			final InputStream ios = null;
			try {
				options = new HadoopLocalFeaturesToolOptions(context.getConfiguration().getStrings(ARGS_KEY));
				options.prepare();
			} finally {
				if (ios != null)
					ios.close();
			}
		}

		@Override
		protected void
				map(Text key, BytesWritable value, Context context) throws IOException, InterruptedException
		{
			try {
				final Timer t = Timer.timer();
				logger.info("Generating Keypoint for image: " + key);
				logger.trace("Keypoint mode: " + options.getMode());
				ByteArrayOutputStream baos = null;
				final LocalFeatureList<? extends LocalFeature<?, ?>> kpl = options.getMode().extract(value.getBytes());

				logger.debug("Keypoints generated! Found: " + kpl.size());
				if (options.dontwrite) {
					logger.trace("Not Writing");
					return;
				}

				logger.trace("Writing");
				baos = new ByteArrayOutputStream();
				if (options.isAsciiMode()) {
					IOUtils.writeASCII(baos, kpl);
				} else {
					IOUtils.writeBinary(baos, kpl);
				}
				context.write(key, new BytesWritable(baos.toByteArray()));
				logger.info("Done in " + t.duration() + "ms");
				context.getCounter(Counters.SUCCESSFUL).increment(1L);
			} catch (final Throwable e) {
				context.getCounter(Counters.FAILED).increment(1L);
				logger.warn("Problem with this image. (" + e + "/" + key + ")");
				e.printStackTrace(System.err);
			}
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		final HadoopLocalFeaturesToolOptions options = new HadoopLocalFeaturesToolOptions(args, true);
		options.prepare();

		final Path[] paths = options.getInputPaths();
		final TextBytesSequenceFileUtility util = new TextBytesSequenceFileUtility(paths[0].toUri(), true);
		final Map<String, String> metadata = new HashMap<String, String>();

		if (util.getUUID() != null)
			metadata.put(MetadataConfiguration.UUID_KEY, util.getUUID());

		metadata.put(MetadataConfiguration.CONTENT_TYPE_KEY, "application/localfeatures-" + options.getMode().name()
				+ "-" + (options.isAsciiMode() ? "ascii" : "bin"));

		final Job job = TextBytesJobUtil.createJob(paths, options.getOutputPath(), metadata, this.getConf());
		job.setJarByClass(this.getClass());

		options.mapperModeOp.prepareJobMapper(job, LocalFeaturesMapper.class);
		job.getConfiguration().setStrings(ARGS_KEY, args);
		job.setNumReduceTasks(0);

		SequenceFileOutputFormat.setCompressOutput(job, !options.dontcompress);

		long start, end;
		start = System.currentTimeMillis();
		job.waitForCompletion(true);
		end = System.currentTimeMillis();

		System.out.println("Took: " + (end - start) + "ms");

		options.serialiseExtractor();

		return 0;
	}

	/**
	 * The main entry point
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ToolRunner.run(new HadoopLocalFeaturesTool(), args);
	}
}
