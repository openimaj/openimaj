package org.openimaj.hadoop.tools.twitter;

import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.openimaj.hadoop.mapreduce.TextBytesJobUtil;
import org.openimaj.hadoop.sequencefile.MetadataConfiguration;
import org.openimaj.hadoop.sequencefile.TextBytesSequenceFileUtility;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.terrier.utility.io.HadoopUtility;

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
