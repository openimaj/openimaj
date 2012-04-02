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

import java.util.HashMap;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.openimaj.hadoop.mapreduce.TextBytesJobUtil;
import org.openimaj.hadoop.sequencefile.TextBytesSequenceFileUtility;
import org.openimaj.hadoop.tools.HadoopToolsUtil;

enum CounterEnum{
	CHEESE,FLEES;
}
public class HadoopCounterTest extends Configured implements Tool{
	
	public static class CounterMapper extends Mapper<Text, BytesWritable, Text, BytesWritable>{
		public CounterMapper() {
			// TODO Auto-generated constructor stub
		}
		@Override
		protected void map(Text key, BytesWritable value, Mapper<Text, BytesWritable, Text, BytesWritable>.Context context) throws java.io.IOException, InterruptedException 
		{
			context.getCounter(CounterEnum.CHEESE).increment(10);
			context.getCounter(CounterEnum.FLEES).increment(20);
			context.write(key, value);
		}
	}
	
	public static class CounterReducer extends Reducer<Text, BytesWritable, Text, BytesWritable>{
		public CounterReducer() {
			// TODO Auto-generated constructor stub
		}
		@Override
		protected void reduce(Text key, Iterable<BytesWritable> values, Context context){
			Counter cheeseCounter = context.getCounter(CounterEnum.CHEESE);
			Counter fleesCounter = context.getCounter(CounterEnum.FLEES);
			System.out.println(cheeseCounter.getName() + ": " + cheeseCounter.getValue());
			System.out.println(fleesCounter.getName() + ": " + fleesCounter.getValue());
			
		}
	}
	@Override
	public int run(String[] args) throws Exception {
//		String clusterFileString = options.getInputString();
		Path[] paths = new Path[]{new Path(args[0])};
		TextBytesSequenceFileUtility util = new TextBytesSequenceFileUtility(paths[0].toUri() , true);
		HadoopToolsUtil.removeFile(args[1]);
		Job job = TextBytesJobUtil.createJob(paths, new Path(args[1]), new HashMap<String,String>(), this.getConf());
		job.setJarByClass(this.getClass());
		job.setMapperClass(CounterMapper.class);
		job.setReducerClass(CounterReducer.class);
		
		SequenceFileOutputFormat.setCompressOutput(job, false);
		long start,end;
		start = System.currentTimeMillis();
		job.waitForCompletion(true);
		end = System.currentTimeMillis();
		System.out.println("Took: " + (end - start) + "ms");
		return 0;
	}

	public static void main(String[] args) throws Exception {
		ToolRunner.run(new HadoopCounterTest(), args);
	}
}
