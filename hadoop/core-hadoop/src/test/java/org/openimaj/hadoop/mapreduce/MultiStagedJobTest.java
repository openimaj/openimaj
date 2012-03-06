package org.openimaj.hadoop.mapreduce;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.junit.Before;
import org.junit.Test;
import org.openimaj.hadoop.mapreduce.MultiStagedJob.Stage;
import org.openimaj.io.FileUtils;

/**
 * Test the MultiStagedJob 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class MultiStagedJobTest {
	static class CountWords extends Mapper<LongWritable, Text, NullWritable, Text> {
		
		@Override
		protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, NullWritable, Text>.Context context) throws java.io.IOException, InterruptedException 
		{
			context.write(NullWritable.get(), new Text(value.toString().split(" ").length + ""));
		}
	}
	static class AddOne extends Mapper<LongWritable, Text, NullWritable, Text> {
		@Override
		protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, NullWritable, Text>.Context context) throws java.io.IOException, InterruptedException 
		{
			Integer intVal = Integer.parseInt(value.toString());
			context.write(NullWritable.get(), new Text((intVal + 1) + ""));
		}
	}
	private File initialFile;
	private File outputFile;
	/**
	 * Prepare the input file and output location
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException{
		initialFile = FileUtils.copyStreamToTemp(MultiStagedJobTest.class.getResourceAsStream("/org/openimaj/hadoop/textfile"), "file", ".txt");
		outputFile = File.createTempFile("out", ".dir");
		outputFile.delete();
		
	}
	/**
	 * Create a simple two stage process which counts the words per line, outputs the word counts per line and adds one to each word count per line.
	 * @throws Exception
	 */
	@Test
	public void testMultipleStages() throws Exception{
		MultiStagedJob mjob = new MultiStagedJob(initialFile.getAbsolutePath(),outputFile.getAbsolutePath(),new String[]{});
		mjob.queueStage(new Stage(){

			@Override
			public String outname() {
				return "countwords";
			}

			@Override
			public Job stage(Path[] inputs, Path output) throws IOException {
				Job job = new Job(new Configuration());
				
				job.setInputFormatClass(TextInputFormat.class);
				job.setOutputKeyClass(NullWritable.class);
				job.setOutputValueClass(Text.class);
				job.setOutputFormatClass(TextOutputFormat.class);
			
				TextInputFormat.setInputPaths(job, inputs);
				TextOutputFormat.setOutputPath(job, output);
				TextOutputFormat.setCompressOutput(job, false);
				job.setMapperClass(CountWords.class);
				return job;
			}
			
		});
		
		mjob.queueStage(new Stage(){

			@Override
			public String outname() {
				return "addone";
			}

			@Override
			public Job stage(Path[] inputs, Path output) throws IOException {
				Job job = new Job(new Configuration());
				
				job.setInputFormatClass(TextInputFormat.class);
				job.setOutputKeyClass(NullWritable.class);
				job.setOutputValueClass(Text.class);
				job.setOutputFormatClass(TextOutputFormat.class);
			
				TextInputFormat.setInputPaths(job, inputs);
				TextOutputFormat.setOutputPath(job, output);
				TextOutputFormat.setCompressOutput(job, false);
				job.setMapperClass(AddOne.class);
				return job;
			}
			
		});
		System.out.println(mjob.runAll());
	}
}
