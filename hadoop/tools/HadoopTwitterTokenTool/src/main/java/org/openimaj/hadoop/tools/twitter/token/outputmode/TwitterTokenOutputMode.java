package org.openimaj.hadoop.tools.twitter.token.outputmode;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenMode;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author ss
 *
 */
public interface TwitterTokenOutputMode {
	/**
	 * @param opts The token tool options
	 * @param completedMode The mode to output from
	 * @param outputPath The final location of the output
	 * @param replace whether to replace an existing output
	 * @throws IOException 
	 * 
	 */
	public void write(
			HadoopTwitterTokenToolOptions opts, 
			TwitterTokenMode completedMode, 
			String outputPath, 
			boolean replace) throws Exception;

}
