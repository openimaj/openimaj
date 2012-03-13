package org.openimaj.hadoop.tools.twitter.token.mode;


import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
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
	},
	/**
	 * Skip the actual processing, assume the input contains the data needed by the output
	 */
	JUST_OUTPUT{
		@Override
		public TwitterTokenMode mode() throws Exception {
			return new TwitterTokenMode(){
				private String[] finalOutput;

				@Override
				public void perform(HadoopTwitterTokenToolOptions opts) throws Exception {
					this.finalOutput = opts.getAllInputs();
				}

				@Override
				public String[] finalOutput(HadoopTwitterTokenToolOptions opts) throws Exception {
					return finalOutput;
				}
				
			};
		}
	},
	;

	@Override
	public Object getOptions() {
		return this;
	}

	/**
	 * @param opts The options the tool was initiated with
	 * @return a TwitterTokenMode associated with this option 
	 * @throws Exception something went wrong!
	 */
	public abstract TwitterTokenMode mode() throws Exception;

	
	
}
