package org.openimaj.hadoop.mapreduce;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.junit.Before;
import org.junit.Test;
import org.openimaj.hadoop.mapreduce.stage.Stage;
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
		mjob.queueStage(new Stage<
				TextInputFormat, 
				TextOutputFormat<NullWritable,Text>,
				LongWritable,Text,
				NullWritable,Text,
				NullWritable,Text
		>(){

			@Override
			public String outname() {
				return "countwords";
			}
			
			@Override
			public Class<? extends Mapper<LongWritable, Text, NullWritable, Text>> mapper() {
				return CountWords.class;
			}
		});
		
		mjob.queueStage(new Stage<
			TextInputFormat, 
			TextOutputFormat<NullWritable,Text>,
			LongWritable,Text,
			NullWritable,Text,
			NullWritable,Text
		>(){

			@Override
			public String outname() {
				return "addone";
			}
			
			@Override
			public Class<? extends Mapper<LongWritable, Text, NullWritable, Text>> mapper() {
				return AddOne.class;
			}
			
		});
		System.out.println(mjob.runAll());
		
		Path[] countwordsp = mjob.getStagePaths("countwords");
		Path[] addonep = mjob.getStagePaths("addone");
		FileSystem fs = getFileSystem(countwordsp[0].toUri());
		InputStream countstream = fs.open(countwordsp[0]);
		InputStream addonestream = fs.open(addonep[0]);
		String[] clines = FileUtils.readlines(countstream);
		String[] alines = FileUtils.readlines(addonestream);
		
		for (int i = 0; i < alines.length; i++) {
			int cint = Integer.parseInt(clines[i]);
			int aint = Integer.parseInt(alines[i]);
			assertTrue(aint == cint + 1);
		}
	}
	private static FileSystem getFileSystem(URI uri) throws IOException {
		Configuration config = new Configuration();
		FileSystem fs = FileSystem.get(uri, config);
		if (fs instanceof LocalFileSystem) fs = ((LocalFileSystem)fs).getRaw();
		return fs;
	}
}
