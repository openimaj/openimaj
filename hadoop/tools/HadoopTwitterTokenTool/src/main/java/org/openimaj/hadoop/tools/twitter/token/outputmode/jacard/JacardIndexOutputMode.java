package org.openimaj.hadoop.tools.twitter.token.outputmode.jacard;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
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
				HadoopToolsUtil.getInputPaths(completedMode.finalOutput(opts) , CountTweetsInTimeperiod.TIMECOUNT_DIR),
				HadoopToolsUtil.getOutputPath(outputPath),
				opts.getArgs()
		);
		stages.queueStage(new TimeIndex().stage());
		stages.runAll();
		LinkedHashMap<Long, IndependentPair<Long, Long>> timeIndex = TimeIndex.readTimeCountLines(outputPath);
		long eldest = 0;
		long diff = 0;
		for (long time : timeIndex.keySet()) {
			diff = time - eldest;
			eldest = time;
		}
		
		this.stages = new MultiStagedJob(
				HadoopToolsUtil.getInputPaths(completedMode.finalOutput(opts) , CountWordsAcrossTimeperiod.WORDCOUNT_DIR),
				HadoopToolsUtil.getOutputPath(outputPath),
				opts.getArgs()
		);
//		stages.queueStage(new CumulativeTimeWord(diff,eldest).stage());
		new CumulativeTimeWord(1,eldest).stage(stages);
		stages.runAll();
	}

}
