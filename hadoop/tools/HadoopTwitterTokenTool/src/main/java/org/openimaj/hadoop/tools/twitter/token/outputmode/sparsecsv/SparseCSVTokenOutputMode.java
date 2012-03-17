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
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.CountTweetsInTimeperiod;
import org.openimaj.hadoop.tools.twitter.token.mode.CountWordsAcrossTimeperiod;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenMode;
import org.openimaj.hadoop.tools.twitter.token.mode.dfidf.DFIDFTokenMode;
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
public class SparseCSVTokenOutputMode extends TwitterTokenOutputMode {

	private MultiStagedJob stages;

	@Override
	public void write(
			HadoopTwitterTokenToolOptions opts, 
			TwitterTokenMode completedMode) throws Exception{
		
		HadoopToolsUtil.validateOutput(outputPath,replace);
		
		this.stages = new MultiStagedJob(
				HadoopToolsUtil.getInputPaths(completedMode.finalOutput(opts) , CountWordsAcrossTimeperiod.WORDCOUNT_DIR),
				HadoopToolsUtil.getOutputPath(outputPath),
				opts.getArgs()
		);
		// Three stage process
		// 1a. Write all the words (word per line)
		stages.queueStage(new WordIndex().stage());
		final Path wordIndex = stages.runAll();
		// 1b. Write all the times (time per line)
		this.stages = new MultiStagedJob(
				HadoopToolsUtil.getInputPaths(completedMode.finalOutput(opts) , CountTweetsInTimeperiod.TIMECOUNT_DIR),
				HadoopToolsUtil.getOutputPath(outputPath),
				opts.getArgs()
		);
		stages.queueStage(new TimeIndex().stage());
		final Path timeIndex = stages.runAll();
		// 3. Write all the values (loading in the words and times)
		
		this.stages = new MultiStagedJob(
				HadoopToolsUtil.getInputPaths(completedMode.finalOutput(opts) , CountWordsAcrossTimeperiod.WORDCOUNT_DIR),
				HadoopToolsUtil.getOutputPath(outputPath),
				opts.getArgs()
		);
		stages.queueStage(new Values(outputPath).stage());
		stages.runAll();
	}


}
