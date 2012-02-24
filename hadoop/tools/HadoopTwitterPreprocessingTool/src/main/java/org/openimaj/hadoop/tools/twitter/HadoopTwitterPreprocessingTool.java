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
package org.openimaj.hadoop.tools.twitter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.kohsuke.args4j.CmdLineException;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.twitter.TwitterStatus;



/**
 * A hadoop implementation of twitter preprocessing
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class HadoopTwitterPreprocessingTool extends Configured implements Tool {
	private static final String ARGS_KEY = "twitter.preprocessing.args";

	static class TwitterPreprocessingMapper extends Mapper<LongWritable, Text, NullWritable, Text> {
		private static HadoopTwitterPreprocessingToolOptions options = null;
		private static List<TwitterPreprocessingMode<?>> modes = null;
		
		protected static synchronized void loadOptions(Mapper<LongWritable, Text, NullWritable, Text>.Context context) throws IOException {
			if (options == null) {
				try {
					options = new HadoopTwitterPreprocessingToolOptions(context.getConfiguration().getStrings(ARGS_KEY));
					options.prepare();
					modes = options.preprocessingMode();
				} catch (CmdLineException e) {
					throw new IOException(e);
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
		}
		
		@Override
		protected void setup(Mapper<LongWritable, Text, NullWritable, Text>.Context context)throws IOException, InterruptedException{
			loadOptions(context);
		}

		@Override
		protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, NullWritable, Text>.Context context) throws java.io.IOException, InterruptedException 
		{
			TwitterStatus status = TwitterStatus.fromString(value.toString());
			for (TwitterPreprocessingMode<?> mode : modes) {
				mode.process(status);
			}
			StringWriter outTweetString = new StringWriter();
			PrintWriter outTweetWriter = new PrintWriter(outTweetString);
			try {
				options.ouputMode().output(status, outTweetWriter );
				context.write(NullWritable.get(), new Text(outTweetString.getBuffer().toString()));
			} catch (Exception e) {
				System.err.println("Failed to write tweet: " + status.text);
				System.err.println("With error: ");
				e.printStackTrace();
			}
		}
	}
	@Override
	public int run(String[] args) throws Exception {
		HadoopTwitterPreprocessingToolOptions options = new HadoopTwitterPreprocessingToolOptions(args,true);
		options.prepare();
		Job job = createJob(options, this.getConf());
		job.setJarByClass(this.getClass());
		options.mapperMode.prepareJobMapper(job,TwitterPreprocessingMapper.class);
		job.getConfiguration().setStrings(ARGS_KEY, args);
		job.waitForCompletion(true);
		return 0;
	}

	private Job createJob(HadoopTwitterPreprocessingToolOptions options,Configuration config) throws IOException {
		Job job = new Job(config);
		
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(Text.class);
		job.setOutputFormatClass(TextOutputFormat.class);
	
		TextInputFormat.setInputPaths(job, options.getInputPaths());
		TextOutputFormat.setOutputPath(job, options.getOutputPath());
		TextOutputFormat.setCompressOutput(job, false);
		
		return job;
	}

	/**
	 * run the tool
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			ToolRunner.run(new HadoopTwitterPreprocessingTool(), args);
		} catch (CmdLineException e) {
			System.err.print(e);
		}
	}
}
