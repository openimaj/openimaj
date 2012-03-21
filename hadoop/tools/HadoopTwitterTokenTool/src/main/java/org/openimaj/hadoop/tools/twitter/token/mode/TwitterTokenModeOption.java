package org.openimaj.hadoop.tools.twitter.token.mode;


import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.dfidf.DFIDFTokenMode;
import org.openimaj.hadoop.tools.twitter.token.mode.match.TokenMatchMode;

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
		public TwitterTokenMode getOptions() {
			return new DFIDFTokenMode();
		}
	},
	/**
	 * In the JSONPath requested locate tweets with terms which contain any of the tokens requested. The tokens may be regex
	 */
	MATCH_TERM{
		@Override
		public TwitterTokenMode getOptions() {
			return new TokenMatchMode();
		}
	},
	/**
	 * Skip the actual processing, assume the input contains the data needed by the output
	 */
	JUST_OUTPUT{
		@Override
		public TwitterTokenMode getOptions()  {
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
	},;
	
	@Override
	public abstract TwitterTokenMode getOptions();
	
}
