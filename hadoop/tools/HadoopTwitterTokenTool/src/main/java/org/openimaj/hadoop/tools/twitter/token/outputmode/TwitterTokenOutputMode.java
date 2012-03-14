package org.openimaj.hadoop.tools.twitter.token.outputmode;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.kohsuke.args4j.Option;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenMode;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author ss
 *
 */
public abstract class TwitterTokenOutputMode {
	
	
	@Option(name="--results-output", aliases="-ro", required=true, usage="Where should the results be outputted?", metaVar="STRING")
	public String outputPath;
	
	@Option(name="--results-output-overwrite", aliases="-rorm", required=false, usage="Where should the results be outputted?", metaVar="STRING")
	public boolean replace = false;
	
	/**
	 * @param opts The token tool options
	 * @param completedMode The mode to output from
	 * @param outputPath The final location of the output
	 * @param replace whether to replace an existing output
	 * @throws IOException 
	 * 
	 */
	public abstract void write(
			HadoopTwitterTokenToolOptions opts, 
			TwitterTokenMode completedMode) throws Exception;

}
