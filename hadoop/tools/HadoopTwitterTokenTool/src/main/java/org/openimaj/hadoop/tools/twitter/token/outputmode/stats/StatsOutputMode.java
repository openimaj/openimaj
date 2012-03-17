package org.openimaj.hadoop.tools.twitter.token.outputmode.stats;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.openimaj.hadoop.mapreduce.MultiStagedJob;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.CountWordsAcrossTimeperiod;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenMode;
import org.openimaj.hadoop.tools.twitter.token.mode.dfidf.DFIDFTokenMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.TwitterTokenOutputMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.WordIndex;
import org.openimaj.util.pair.IndependentPair;

public class StatsOutputMode extends TwitterTokenOutputMode {

	private MultiStagedJob stages;

	@Override
	public void write(HadoopTwitterTokenToolOptions opts,TwitterTokenMode completedMode) throws Exception {
 		
		this.stages = new MultiStagedJob(
				HadoopToolsUtil.getInputPaths(completedMode.finalOutput(opts),CountWordsAcrossTimeperiod.WORDCOUNT_DIR),
				HadoopToolsUtil.getOutputPath(outputPath),
				opts.getArgs()
		);
		// Three stage process
		// 1a. Write all the words (word per line)
		stages.queueStage(new WordIndex().stage());
		final Path wordIndex = stages.runAll();
		
		HashMap<String, IndependentPair<Long, Long>> wordCountLines = WordIndex.readWordCountLines(wordIndex.toString(),"");
		StatsWordMatch matches = new StatsWordMatch();
		for (Entry<String, IndependentPair<Long, Long>> entry : wordCountLines.entrySet()) {
			String word = entry.getKey();
			IndependentPair<Long, Long> countLine = entry.getValue();
			Long count = countLine.firstObject();	
			matches.updateStats(word, count);
		}
		
		System.out.println(matches);
	}

}
