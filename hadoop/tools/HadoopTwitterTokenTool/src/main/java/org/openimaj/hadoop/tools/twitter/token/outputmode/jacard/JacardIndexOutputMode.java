package org.openimaj.hadoop.tools.twitter.token.outputmode.jacard;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.openimaj.hadoop.mapreduce.MultiStagedJob;
import org.openimaj.hadoop.mapreduce.MultiStagedJob.Stage;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.CountTweetsInTimeperiod;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenMode;
import org.openimaj.hadoop.tools.twitter.token.mode.dfidf.DFIDFTokenMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.TwitterTokenOutputMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.TimeIndex;
import org.openimaj.util.pair.IndependentPair;

public class JacardIndexOutputMode extends TwitterTokenOutputMode {

	private MultiStagedJob stages;

	@Override
	public void write(HadoopTwitterTokenToolOptions opts,TwitterTokenMode completedMode) throws Exception {
		/**
		 * Two stage process:
		 * 	generate the time index (to find out the biggest time and interval)
		 * 	use the biggest time and interval to launch the jaccard index job:
		 * 		for each word at a time, emmit once per time between its time till biggest time
		 * 		reduce: for each time, use all words before instance to difference between words at instance
		 */
		this.stages = new MultiStagedJob(
				HadoopToolsUtil.getInputPaths(completedMode.finalOutput(opts) , DFIDFTokenMode.TIMECOUNT_DIR),
				HadoopToolsUtil.getOutputPath(outputPath),
				opts.getArgs()
		);
		stages.queueStage(new Stage() {
			@Override
			public Job stage(Path[] inputs, Path output, Configuration conf) throws IOException {
				Job job = new Job(conf);
				
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
		stages.runAll();
		LinkedHashMap<Long, IndependentPair<Long, Long>> timeIndex = TimeIndex.readTimeCountLines(outputPath);
		final long[] eldest = new long[1];
		final long[] diff = new long[1];
		for (long time : timeIndex.keySet()) {
			diff[0] = time - eldest[0];
			eldest[0] = time;
		}
		
		this.stages = new MultiStagedJob(
				HadoopToolsUtil.getInputPaths(completedMode.finalOutput(opts) , DFIDFTokenMode.WORDCOUNT_DIR),
				HadoopToolsUtil.getOutputPath(outputPath),
				opts.getArgs()
		);
		stages.queueStage(new Stage() {
			@Override
			public Job stage(Path[] inputs, Path output, Configuration conf) throws IOException {
				Job job = new Job(conf);
				
				job.setInputFormatClass(SequenceFileInputFormat.class);
				job.setOutputKeyClass(LongWritable.class);
				job.setOutputValueClass(Text.class);
				job.setOutputFormatClass(TextOutputFormat.class);
				job.setJarByClass(this.getClass());
			
				SequenceFileInputFormat.setInputPaths(job, inputs);
				TextOutputFormat.setOutputPath(job, output);
				TextOutputFormat.setCompressOutput(job, false);
				job.setMapperClass(CumulativeTimeWord.Map.class);
				job.setReducerClass(CumulativeTimeWord.Reduce.class);
				job.getConfiguration().setLong(CumulativeTimeWord.TIME_DELTA, diff[0]);
				job.getConfiguration().setLong(CumulativeTimeWord.TIME_ELDEST, eldest[0]);
				return job;
			}
			
			@Override
			public String outname() {
				return "times";
			}
		});
	}

}
