package org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.openimaj.hadoop.mapreduce.MultiStagedJob;
import org.openimaj.hadoop.mapreduce.MultiStagedJob.Stage;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.CountWordsAcrossTimeperiod;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.TwitterTokenOutputMode;

/**
 * Create a sparse CSV token output. The directory created contains 3 files:
 * 	words/ : contains a list of words ordered by count across all time. 
 * 	times/ : contains a list of times ordered by count of all tweets
 * 	values/ : a list of (wordIndex,timeIndex,wordTimeCount,tweetTimeCount,tweetCount,wordCount)
 * 
 * @author ss
 *
 */
public class SparseCSVTokenOutputMode implements TwitterTokenOutputMode {

	private MultiStagedJob stages;

	@Override
	public void write(
			HadoopTwitterTokenToolOptions opts, 
			TwitterTokenMode completedMode, 
			final String outputPath,
			boolean replace) throws Exception{
		
		HadoopToolsUtil.validateOutput(outputPath,replace);
		
		this.stages = new MultiStagedJob(
				HadoopToolsUtil.getInputPaths(completedMode.finalOutput(opts)),
				HadoopToolsUtil.getOutputPath(outputPath)
		);
		// Three stage process
		// 1a. Write all the words (word per line)
		stages.queueStage(new Stage() {
			@Override
			public Job stage(Path[] inputs, Path output) throws IOException {
				Job job = new Job(new Configuration());
				
				job.setInputFormatClass(SequenceFileInputFormat.class);
				job.setOutputKeyClass(LongWritable.class);
				job.setOutputValueClass(Text.class);
				job.setOutputFormatClass(TextOutputFormat.class);
				job.setJarByClass(this.getClass());
			
				SequenceFileInputFormat.setInputPaths(job, inputs);
				TextOutputFormat.setOutputPath(job, output);
				TextOutputFormat.setCompressOutput(job, false);
				job.setMapperClass(WordIndex.Map.class);
				job.setReducerClass(WordIndex.Reduce.class);
				job.setSortComparatorClass(LongWritable.Comparator.class);
				job.setNumReduceTasks(1);
				return job;
			}
			
			@Override
			public String outname() {
				return "words";
			}
		});
		final Path wordIndex = stages.runAll();
		// 1b. Write all the times (time per line)
		this.stages = new MultiStagedJob(
				HadoopToolsUtil.getInputPaths(completedMode.finalOutput(opts)),
				HadoopToolsUtil.getOutputPath(outputPath)
		);
		stages.queueStage(new Stage() {
			@Override
			public Job stage(Path[] inputs, Path output) throws IOException {
				Job job = new Job(new Configuration());
				
				job.setInputFormatClass(SequenceFileInputFormat.class);
				job.setOutputKeyClass(LongWritable.class);
				job.setOutputValueClass(LongWritable.class);
				job.setOutputFormatClass(TextOutputFormat.class);
				job.setJarByClass(this.getClass());
			
				SequenceFileInputFormat.setInputPaths(job, inputs);
				TextOutputFormat.setOutputPath(job, output);
				TextOutputFormat.setCompressOutput(job, false);
				job.setMapperClass(TimeIndex.Map.class);
				job.setReducerClass(TimeIndex.Reduce.class);
				job.setSortComparatorClass(LongWritable.Comparator.class);
				job.setNumReduceTasks(1);
				return job;
			}
			
			@Override
			public String outname() {
				return "times";
			}
		});
		final Path timeIndex = stages.runAll();
		// 3. Write all the values (loading in the words and times)
		
		this.stages = new MultiStagedJob(
				HadoopToolsUtil.getInputPaths(completedMode.finalOutput(opts)),
				HadoopToolsUtil.getOutputPath(outputPath)
		);
		stages.queueStage(new Stage() {
			@Override
			public Job stage(Path[] inputs, Path output) throws IOException {
				Job job = new Job(new Configuration());
				
				job.setInputFormatClass(SequenceFileInputFormat.class);
				job.setOutputKeyClass(NullWritable.class);
				job.setOutputValueClass(Text.class);
				job.setOutputFormatClass(TextOutputFormat.class);
				job.setJarByClass(this.getClass());
			
				SequenceFileInputFormat.setInputPaths(job, inputs);
				TextOutputFormat.setOutputPath(job, output);
				TextOutputFormat.setCompressOutput(job, false);
				job.setMapperClass(Values.Map.class);
				job.setReducerClass(Values.Reduce.class);
				job.setNumReduceTasks(1);
				job.getConfiguration().setStrings(Values.ARGS_KEY, new String[]{outputPath.toString()});
				return job;
			}
			
			@Override
			public String outname() {
				return "values";
			}
		});
		stages.runAll();
	}


}
