package org.openimaj.hadoop.tools.twitter.token.mode;

import org.kohsuke.args4j.CmdLineOptionsProvider;

/**
 * A twitter tweet token counting mode
 * 
 * @author ss
 *
 */
public enum TwitterTokenModeOption implements CmdLineOptionsProvider{
	/**
	 * Calculates DF-IDF for each term as described by: "Event Detection in Twitter" by J. Weng et. al. 
	 */
	DFIDF;

	@Override
	public Object getOptions() {
		return this;
	}
	
	
}
