package org.openimaj.tools.twitter.modes.output;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingModeOption;

/**
 * Control how twitter analysis should be outputted
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 *
 *
 */
public enum TwitterOutputModeOption  implements CmdLineOptionsProvider {
	/**
	 * Appends the analysis to tweets
	 */
	APPEND {
		@Override
		public TwitterOutputMode createMode(List<TwitterPreprocessingModeOption> twitterPreprocessingModes) {
			return new SelectiveAnalysisOutputMode();
		}
	},
	/**
	 * just the analysis, no tweet
	 */
	SIMPLE {
		@Override
		public TwitterOutputMode createMode(List<TwitterPreprocessingModeOption> twitterPreprocessingModes) {
			List<String> analysisKeys = new ArrayList<String>();
			for (TwitterPreprocessingModeOption mode : twitterPreprocessingModes) {
				analysisKeys.add(mode.getAnalysisKey());
			}
			return new SelectiveAnalysisOutputMode(analysisKeys);
		}
	};
	
	@Override
	public Object getOptions() {return this;}

	/**
	 * @param twitterPreprocessingModes the processing modes
	 * @return an output mode
	 */
	public abstract TwitterOutputMode createMode(List<TwitterPreprocessingModeOption> twitterPreprocessingModes);	
}