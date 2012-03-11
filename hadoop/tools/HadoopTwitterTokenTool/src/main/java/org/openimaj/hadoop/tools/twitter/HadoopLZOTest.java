package org.openimaj.hadoop.tools.twitter;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.text.nlp.TweetTokeniser;
import org.openimaj.text.nlp.TweetTokeniserException;
import org.openimaj.twitter.TwitterStatus;

import com.hadoop.mapreduce.LzoTextInputFormat;


public class HadoopLZOTest extends Configured implements Tool{
	enum CounterEnum{
		CHEESE,FLEES;
	}
	public static class CounterMapper extends Mapper<LongWritable, Text, LongWritable, Text>{
		public CounterMapper() {
			// TODO Auto-generated constructor stub
		}
		@Override
		protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, LongWritable, Text>.Context context) throws java.io.IOException, InterruptedException 
		{
			TwitterStatus status = TwitterStatus.fromJSONString(value.toString());
			
			context.getCounter(CounterEnum.CHEESE).increment(10);
			context.getCounter(CounterEnum.FLEES).increment(20);
			if(status.isInvalid())return;
			try {
				TweetTokeniser tok = new TweetTokeniser(status.text);
				context.write(key, new Text(StringUtils.join(tok.getTokens(),",")));
			} catch (TweetTokeniserException e) {
			}
		}
	}
	
	public static class CounterReducer extends Reducer<LongWritable, Text, NullWritable, Text>{
		public CounterReducer() {
			// TODO Auto-generated constructor stub
		}
		@Override
		protected void reduce(LongWritable key, Iterable<Text> values, Reducer<LongWritable, Text, NullWritable, Text>.Context context){
			Counter cheeseCounter = context.getCounter(CounterEnum.CHEESE);
			Counter fleesCounter = context.getCounter(CounterEnum.FLEES);
			System.out.println(cheeseCounter.getName() + ": " + cheeseCounter.getValue());
			System.out.println(fleesCounter.getName() + ": " + fleesCounter.getValue());
			for (Text text : values) {
				try {
					context.write(NullWritable.get(), text);
				} catch (IOException e) {
				} catch (InterruptedException e) {
				}
				
			}
			
			
		}
	}
	@Override
	public int run(String[] args) throws Exception {
		Path[] paths = new Path[]{new Path(args[0])};
		Path out = new Path(args[1]);
		HadoopToolsUtil.validateOutput(args[1], true);
		Job job = new Job(this.getConf());
		
		job.setInputFormatClass(LzoTextInputFormat.class);
		job.setOutputKeyClass(LongWritable.class);
		job.setOutputValueClass(Text.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setJarByClass(this.getClass());
	
		LzoTextInputFormat.setInputPaths(job, paths);
		TextOutputFormat.setOutputPath(job, out);
		job.setMapperClass(CounterMapper.class);
		job.setReducerClass(CounterReducer.class);
		
		
		long start,end;
		start = System.currentTimeMillis();
		job.waitForCompletion(true);
		end = System.currentTimeMillis();
		System.out.println("Took: " + (end - start) + "ms");
		return 0;
	}

	public static void main(String[] args) throws Exception {
		ToolRunner.run(new HadoopLZOTest(), args);
	}
}
