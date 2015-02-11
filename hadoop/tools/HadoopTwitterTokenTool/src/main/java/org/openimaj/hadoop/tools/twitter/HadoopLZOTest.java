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

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.text.nlp.TweetTokeniser;
import org.openimaj.text.nlp.TweetTokeniserException;
import org.openimaj.twitter.GeneralJSONTwitter;
import org.openimaj.twitter.USMFStatus;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class HadoopLZOTest extends Configured implements Tool {
	enum CounterEnum {
		CHEESE, FLEES;
	}

	public static class CounterMapper extends Mapper<LongWritable, Text, LongWritable, Text> {
		public CounterMapper() {
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, LongWritable, Text>.Context context)
				throws java.io.IOException, InterruptedException
		{
			final USMFStatus status = new USMFStatus(GeneralJSONTwitter.class);
			status.fillFromString(value.toString());

			context.getCounter(CounterEnum.CHEESE).increment(10);
			context.getCounter(CounterEnum.FLEES).increment(20);
			if (status.isInvalid())
				return;
			try {
				final TweetTokeniser tok = new TweetTokeniser(status.text);
				context.write(key, new Text(StringUtils.join(tok.getTokens(), ",")));
			} catch (final TweetTokeniserException e) {
			}
		}
	}

	public static class CounterReducer extends Reducer<LongWritable, Text, NullWritable, Text> {
		public CounterReducer() {
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void reduce(LongWritable key, Iterable<Text> values,
				Reducer<LongWritable, Text, NullWritable, Text>.Context context)
		{
			final Counter cheeseCounter = context.getCounter(CounterEnum.CHEESE);
			final Counter fleesCounter = context.getCounter(CounterEnum.FLEES);
			System.out.println(cheeseCounter.getName() + ": " + cheeseCounter.getValue());
			System.out.println(fleesCounter.getName() + ": " + fleesCounter.getValue());
			for (final Text text : values) {
				try {
					context.write(NullWritable.get(), text);
				} catch (final IOException e) {
				} catch (final InterruptedException e) {
				}

			}

		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public int run(String[] args) throws Exception {
		Class<? extends InputFormat<?, ?>> lzoClass = null;
		try {
			lzoClass = (Class<? extends InputFormat<?, ?>>) Class.forName("com.hadoop.mapreduce.LzoTextInputFormat");
		} catch (final ClassNotFoundException nfe) {
			System.err.println("LZO not installed; skipping");
			return -1;
		}

		final Path[] paths = new Path[] { new Path(args[0]) };
		final Path out = new Path(args[1]);
		HadoopToolsUtil.validateOutput(args[1], true);
		final Job job = new Job(this.getConf());

		job.setInputFormatClass(lzoClass);
		job.setOutputKeyClass(LongWritable.class);
		job.setOutputValueClass(Text.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setJarByClass(this.getClass());

		lzoClass.getMethod("setInputPaths", Path[].class).invoke(null, (Object[]) paths);
		TextOutputFormat.setOutputPath(job, out);
		job.setMapperClass(CounterMapper.class);
		job.setReducerClass(CounterReducer.class);

		long start, end;
		start = System.currentTimeMillis();
		job.waitForCompletion(true);
		end = System.currentTimeMillis();
		System.out.println("Took: " + (end - start) + "ms");
		return 0;
	}
}
