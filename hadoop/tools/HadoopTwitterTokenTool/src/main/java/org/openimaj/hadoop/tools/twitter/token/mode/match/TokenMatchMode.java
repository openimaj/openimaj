package org.openimaj.hadoop.tools.twitter.token.mode.match;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.Path;
import org.hsqldb.lib.InOutUtil;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.hadoop.mapreduce.MultiStagedJob;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenMode;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenModeOption;
import org.openimaj.tools.InOutToolOptions;

public class TokenMatchMode implements TwitterTokenMode {

	@Option(name="--regex-match", aliases="-r", required=false, usage="Tokens which match this regex", multiValued=true)
	private List<String> tomatch = new ArrayList<String>();
	
	private MultiStagedJob stages;
	
	@Override
	public void perform(HadoopTwitterTokenToolOptions opts) throws Exception {
		Path outpath = HadoopToolsUtil.getOutputPath(opts);
		this.stages = new MultiStagedJob(HadoopToolsUtil.getInputPaths(opts),outpath,opts.getArgs());
		InOutToolOptions.prepareMultivaluedArgument(tomatch,"^.*$");
		
		this.stages.queueStage(new TokenRegexStage(tomatch,opts.getNonHadoopArgs()));
		this.stages.runAll();
	}

	@Override
	public String[] finalOutput(HadoopTwitterTokenToolOptions opts)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
