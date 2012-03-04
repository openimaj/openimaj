package org.openimaj.hadoop.tools.twitter.token.mode;

import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;

/**
 * A method of extracting tokens from twitter and outputting them in some way
 * @author ss
 *
 */
public interface TwitterTokenMode {
	/**
	 * @param opts
	 * @throws Exception something went wrong!
	 */
	public abstract void perform(HadoopTwitterTokenToolOptions opts) throws Exception;
	
	/**
	 * Drive the provided output mode with the final product of the tool
	 * 
	 * @param opts the output can be found in opts.outputMode()
	 * @throws Exception
	 */
	public abstract void output(HadoopTwitterTokenToolOptions opts) throws Exception;
}
