package org.openimaj.hadoop.tools.twitter.token.outputmode.jacard;

import org.openimaj.hadoop.mapreduce.MultiStagedJob;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.CountWordsAcrossTimeperiod;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.TwitterTokenOutputMode;

public class JacardIndexOutputMode extends TwitterTokenOutputMode {

	@Override
	public void write(HadoopTwitterTokenToolOptions opts,TwitterTokenMode completedMode) throws Exception {
		MultiStagedJob stages;
	
		stages = new MultiStagedJob(
				HadoopToolsUtil.getInputPaths(completedMode.finalOutput(opts) , CountWordsAcrossTimeperiod.WORDCOUNT_DIR),
				HadoopToolsUtil.getOutputPath(outputPath),
				opts.getArgs()
		);
		stages.queueStage(new TimeWordJacardIndex());
		stages.runAll();
	}
}
