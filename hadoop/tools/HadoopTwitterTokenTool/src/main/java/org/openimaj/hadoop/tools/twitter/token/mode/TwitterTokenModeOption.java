package org.openimaj.hadoop.tools.twitter.token.mode;


import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.openimaj.hadoop.tools.twitter.token.mode.dfidf.DFIDFTokenMode;

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
	DFIDF {
		@Override
		public TwitterTokenMode mode() throws Exception {
			return new DFIDFTokenMode();
		}
	};

	@Override
	public Object getOptions() {
		return this;
	}

	/**
	 * @return a TwitterTokenMode associated with this option 
	 * @throws Exception something went wrong!
	 */
	public abstract TwitterTokenMode mode() throws Exception;
	
}
