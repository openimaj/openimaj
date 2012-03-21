package org.openimaj.hadoop.tools.twitter.token.outputmode.correlation;

import java.util.LinkedHashMap;

import org.openimaj.hadoop.mapreduce.MultiStagedJob;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.CountTweetsInTimeperiod;
import org.openimaj.hadoop.tools.twitter.token.mode.CountWordsAcrossTimeperiod;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.TwitterTokenOutputMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.jacard.SingleReducerTimeWord;
import org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.TimeIndex;
import org.openimaj.util.pair.IndependentPair;

public class CorrelationOutputMode extends TwitterTokenOutputMode {

	@Override
	public void write(HadoopTwitterTokenToolOptions opts,TwitterTokenMode completedMode) throws Exception {
		
		MultiStagedJob stages = new MultiStagedJob(
				HadoopToolsUtil.getInputPaths(completedMode.finalOutput(opts) , CountTweetsInTimeperiod.TIMECOUNT_DIR),
				HadoopToolsUtil.getOutputPath(outputPath),
				opts.getArgs()
		);
		/**
		 * Two stage process:
		 * 	generate the time index (to find out the biggest time and interval)
		 * 	use the biggest time and interval to launch the jaccard index job:
		 * 		for each word at a time, emmit once per time between its time till biggest time
		 * 		reduce: for each time, use all words before instance to difference between words at instance
		 */
		stages.queueStage(new TimeIndex().stage());
		stages.runAll();
		LinkedHashMap<Long, IndependentPair<Long, Long>> timeIndex = TimeIndex.readTimeCountLines(outputPath);
		long youngest = -1;
		long eldest = 0;
		for (long time : timeIndex.keySet()) {
			if(youngest == -1) youngest = time;
			eldest = time;
		}
		
		stages = new MultiStagedJob(
				HadoopToolsUtil.getInputPaths(completedMode.finalOutput(opts) , CountWordsAcrossTimeperiod.WORDCOUNT_DIR),
				HadoopToolsUtil.getOutputPath(outputPath),
				opts.getArgs()
		);
		
//		stages.queueStage(new CorrelationMapReduce(youngest,eldest));
		stages.runAll();
	}

}
