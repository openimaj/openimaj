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
package org.openimaj.hadoop.tools.exif;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.openimaj.hadoop.mapreduce.TextBytesJobUtil;
import org.openimaj.hadoop.sequencefile.MetadataConfiguration;
import org.openimaj.hadoop.sequencefile.TextBytesSequenceFileUtility;

import com.thebuzzmedia.exiftool.RDFExifTool;
/**
 * An EXIF extraction tool based on exiftool. Allows the location of exiftool on each machine to be specified. 
 * Loads the images from a sequence file of <imageName,image>, loads each image into a temporary file, runs exif tool
 * and outputs the exif information as another sequence file of <imageName, exifData> where exifData is <KEY "VALUE"\n,> 
 * 
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class HadoopEXIF extends Configured implements Tool{
	private static final String ARGS_KEY = "clusterquantiser.args";

	public static class HadoopEXIFMapper extends Mapper<Text, BytesWritable, Text, BytesWritable>{
		
		
		private RDFExifTool tool;
		private HadoopEXIFOptions options;
//		private static ExifTool tool;
		public HadoopEXIFMapper(){}
		
		@Override
		protected void setup(Mapper<Text, BytesWritable, Text, BytesWritable>.Context context)throws IOException, InterruptedException {
			options = new HadoopEXIFOptions(context.getConfiguration().getStrings(ARGS_KEY),false);
			options.prepare();
			System.setProperty("exiftool.path",options.getExifPath());
			tool = new RDFExifTool(options.getInputString());
		}
//		
//		private synchronized static void loadExifTool(Mapper<Text, BytesWritable, Text, BytesWritable>.Context context) {
//			if(tool==null){
//				HadoopEXIFOptions options = new HadoopEXIFOptions(context.getConfiguration().getStrings(ARGS_KEY),false);
//				options.prepare();
//				System.setProperty("exiftool.path",options.getExifPath());
//				tool = new ExifTool();
//			}
//		}

		@Override
		protected void map(Text key, BytesWritable value, Mapper<Text, BytesWritable, Text, BytesWritable>.Context context) throws java.io.IOException, InterruptedException 
		{
			try{
				File tmp = File.createTempFile("prefix", ".image", new File("/tmp")); 
				FileOutputStream fos = new FileOutputStream(tmp);
				IOUtils.copyBytes(new ByteArrayInputStream(value.getBytes()), fos, context.getConfiguration());
				fos.close();
				
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				PrintWriter pw = new PrintWriter(bos);
				options.getOutputMode().output(pw, tmp, key.toString(), tool);
				tmp.delete();
				
				
				context.write(key, new BytesWritable(bos.toByteArray()));
			} catch(Throwable e) {
				System.err.println("... Problem with this image! Keeping Calm. Carrying on.");
				e.printStackTrace(System.err);
			}
		}
	}
	
	@Override
	public int run(String[] args) throws Exception {
		HadoopEXIFOptions options = new HadoopEXIFOptions(args,true);
		options.prepare();
//		String clusterFileString = options.getInputString();
		Path[] paths = options.getInputPaths();
		TextBytesSequenceFileUtility util = new TextBytesSequenceFileUtility(paths[0].toUri() , true);
		Map<String,String> metadata = new HashMap<String,String>();
		if (util.getUUID() != null) metadata.put(MetadataConfiguration.UUID_KEY, util.getUUID());
		metadata.put(MetadataConfiguration.CONTENT_TYPE_KEY, "application/imageexif");
		
		Job job = TextBytesJobUtil.createJob(paths, options.getOutputPath(), metadata,this.getConf());
//		job.setOutputValueClass(Text.class);
		job.setJarByClass(this.getClass());
		job.setMapperClass(HadoopEXIF.HadoopEXIFMapper.class);
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
		ToolRunner.run(new HadoopEXIF(), args);
	}
}
