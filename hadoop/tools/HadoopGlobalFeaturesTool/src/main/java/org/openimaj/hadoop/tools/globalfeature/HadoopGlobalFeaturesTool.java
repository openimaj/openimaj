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
package org.openimaj.hadoop.tools.globalfeature;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.openimaj.feature.FeatureVector;
import org.openimaj.hadoop.mapreduce.TextBytesJobUtil;
import org.openimaj.hadoop.sequencefile.MetadataConfiguration;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.io.IOUtils;

/**
 * A Hadoop version of the GlobalFeaturesTool. Capable of 
 * extracting global image features on very large scale corpora
 * from images stored in {@link SequenceFile}s.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class HadoopGlobalFeaturesTool extends Configured implements Tool 
{
	private static final String ARGS_KEY = "globalfeatures.args";
	private static Logger logger = Logger.getLogger(HadoopGlobalFeaturesTool.class);

	static class GlobalFeaturesMapper extends Mapper<Text, BytesWritable, Text, BytesWritable> {
		private HadoopGlobalFeaturesOptions options;

		public GlobalFeaturesMapper() {}

		@Override
		protected void setup(Mapper<Text, BytesWritable, Text, BytesWritable>.Context context) {
			options = new HadoopGlobalFeaturesOptions(context.getConfiguration().getStrings(ARGS_KEY)); 
		}

		@Override
		protected void map(Text key, BytesWritable value, Mapper<Text, BytesWritable, Text, BytesWritable>.Context context) throws InterruptedException {
			try {
				MBFImage img = ImageUtilities.readMBF(new ByteArrayInputStream(value.getBytes()));
				FeatureVector fv = options.featureOp.extract(img);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				if (options.binary)
					IOUtils.writeBinary(baos, fv);
				else
					IOUtils.writeASCII(baos, fv);

				context.write(key, new BytesWritable(baos.toByteArray()));
			} catch (Exception e) {
				logger.warn("Problem processing image " + key + " (" + e + ")");
			} 
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		HadoopGlobalFeaturesOptions options = new HadoopGlobalFeaturesOptions(args,true); 

		Map<String,String> metadata = new HashMap<String,String>();
		metadata.put(MetadataConfiguration.CONTENT_TYPE_KEY, "application/globalfeature-" + options.feature + "-" + (options.binary? "bin" : "ascii" ));

		metadata.put("clusterquantiser.filetype", (options.binary ? "bin" : "ascii" ));
		
		List<Path> allPaths = new ArrayList<Path>();
		for (String p : options.input) {
			allPaths.addAll(Arrays.asList(HadoopToolsUtil.getInputPaths(p)));
		}
		
		Job job = TextBytesJobUtil.createJob(allPaths, new Path(options.output), metadata, this.getConf());
		job.setJarByClass(this.getClass());
		job.setMapperClass(GlobalFeaturesMapper.class);
		job.getConfiguration().setStrings(ARGS_KEY, args);
		job.setNumReduceTasks(0);

		job.waitForCompletion(true);
		
		return 0;
	}

	/**
	 * The main method for the tool.
	 * @param args the command-line arguments
	 * @throws Exception if an error occurs
	 */
	public static void main( String[] args ) throws Exception
	{
		ToolRunner.run(new HadoopGlobalFeaturesTool(), args);
	}
}
