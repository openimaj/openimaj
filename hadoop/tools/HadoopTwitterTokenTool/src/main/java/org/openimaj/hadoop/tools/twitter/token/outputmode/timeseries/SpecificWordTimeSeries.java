package org.openimaj.hadoop.tools.twitter.token.outputmode.timeseries;

import java.util.List;

import org.apache.hadoop.fs.Path;
import org.kohsuke.args4j.Option;
import org.openimaj.hadoop.mapreduce.MultiStagedJob;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.CountWordsAcrossTimeperiod;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.TwitterTokenOutputMode;

public class SpecificWordTimeSeries extends TwitterTokenOutputMode{
	
	@Option(name="--word-time-series", aliases="-wt", required=false, usage="Construct a time series of each word specified", multiValued = true)
	List<String> wordtimeseries;
	private MultiStagedJob stages;
	
	@Override
	public void write(HadoopTwitterTokenToolOptions opts,TwitterTokenMode completedMode) throws Exception {
		HadoopToolsUtil.validateOutput(outputPath,replace);
		String[] input = completedMode.finalOutput(opts);
		
		this.stages = new MultiStagedJob(
				HadoopToolsUtil.getInputPaths(completedMode.finalOutput(opts) , CountWordsAcrossTimeperiod.WORDCOUNT_DIR),
				HadoopToolsUtil.getOutputPath(outputPath),
				opts.getArgs()
		);
		
		SpecificWordStageProvider swsp = new SpecificWordStageProvider(wordtimeseries);
		this.stages.queueStage(swsp.stage());
		this.stages.runAll();
	}

}
