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
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.FImageBytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.hadoop.mapreduce.TextBytesJobUtil;
import org.openimaj.hadoop.sequencefile.MetadataConfiguration;
import org.openimaj.hadoop.sequencefile.TextBytesSequenceFileUtility;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.io.IOUtils;


public class HadoopLocalFeaturesTool extends Configured implements Tool {
	private static final String ARGS_KEY = "clusterquantiser.args";
	
	static class JKeypointMapper extends Mapper<Text, BytesWritable, Text, BytesWritable> {
		public DoGSIFTEngine egn = null;
		private HadoopLocalFeaturesToolOptions options;

		public JKeypointMapper() {}
		
		@Override
		protected void setup(Mapper<Text, BytesWritable, Text, BytesWritable>.Context context)throws IOException, InterruptedException{
			try {
				Field f = TaskInputOutputContext.class.getDeclaredField("output");
				f.setAccessible(true);
				System.out.println("output" + f.get(context));
				System.out.println("outputClass" + f.get(context).getClass());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			InputStream ios = null;
			try{
				options = new HadoopLocalFeaturesToolOptions(context.getConfiguration().getStrings(ARGS_KEY));
				options.prepare();
				egn = new DoGSIFTEngine();
			}
			finally{
				if(ios!=null) ios.close();
			}
			
		}
		
		@Override
		protected void map(Text key, BytesWritable value, Mapper<Text, BytesWritable, Text, BytesWritable>.Context context) throws java.io.IOException, InterruptedException 
		{
			try{
				System.err.println("Generating Keypoint for image: " + key);
				System.err.println("... Keypoint mode: " + options.getMode());
				ByteArrayOutputStream baos = null;
				LocalFeatureList<? extends LocalFeature<?>> kpl = null;
				if(value instanceof FImageBytesWritable){
					kpl = options.getMode().getKeypointList(((FImageBytesWritable)value).image);
				}
				else{
					kpl = options.getMode().getKeypointList(value.getBytes());
				}
				
				System.err.println("... Keypoints generated! Found: " + kpl.size());
				if(options.dontwrite){
					System.out.println("... Not Writing");
					return;
				}
				
				System.err.println("... Writing ");
				baos = new ByteArrayOutputStream();
				if (options.isAsciiMode()) {
					IOUtils.writeASCII(baos, kpl);
				} else {
					IOUtils.writeBinary(baos, kpl);
				}
				context.write(key, new BytesWritable(baos.toByteArray()));
				System.err.println("... Done!");
			}
			catch(Throwable e){
				System.err.println("... Problem with this image! Keeping Calm. Carrying on.");
				e.printStackTrace(System.err);
			}
		}
	}
	
	@Override
	public int run(String[] args) throws Exception {
		HadoopLocalFeaturesToolOptions options = new HadoopLocalFeaturesToolOptions(args,true);
		options.prepare();
//		String clusterFileString = options.getInputString();
		Path[] paths = options.getInputPaths();
		TextBytesSequenceFileUtility util = new TextBytesSequenceFileUtility(paths[0].toUri() , true);
		Map<String,String> metadata = new HashMap<String,String>();
		if (util.getUUID() != null) metadata.put(MetadataConfiguration.UUID_KEY, util.getUUID());
		metadata.put(MetadataConfiguration.CONTENT_TYPE_KEY, "application/siftkeypoints-" + (options.isAsciiMode() ? "ascii" : "bin" ));
		metadata.put("clusterquantiser.filetype", (options.isAsciiMode() ? "ascii" : "bin" ));
		
		Job job = TextBytesJobUtil.createJob(paths, options.getOutputPath(), metadata,this.getConf());
		job.setJarByClass(this.getClass());
		options.mapperModeOp.prepareJobMapper(job,JKeypointMapper.class);
		job.getConfiguration().setStrings(ARGS_KEY, args);
		job.setNumReduceTasks(0);
		
		SequenceFileOutputFormat.setCompressOutput(job, false);
		long start,end;
		start = System.currentTimeMillis();
		job.waitForCompletion(true);
		end = System.currentTimeMillis();
		System.out.println("Took: " + (end - start) + "ms");
		return 0;
	}

	public static void main(String[] args) throws Exception {
		ToolRunner.run(new HadoopLocalFeaturesTool(), args);
	}
}
