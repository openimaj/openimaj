package org.openimaj.tools.twitter.modes;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.openimaj.tools.twitter.modes.output.SelectiveAnalysisOutputMode;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;

public enum TwitterOutputModeOption  implements CmdLineOptionsProvider {
	APPEND {
		@Override
		public TwitterOutputMode createMode(TwitterPreprocessingMode twitterPreprocessingMode) {
			return new SelectiveAnalysisOutputMode();
		}
	},
	SIMPLE {
		@Override
		public TwitterOutputMode createMode(TwitterPreprocessingMode twitterPreprocessingMode) {
			return new SelectiveAnalysisOutputMode(twitterPreprocessingMode.getAnalysisKeys());
		}
	};
	
	@Override
	public Object getOptions() {return this;}

	public abstract TwitterOutputMode createMode(TwitterPreprocessingMode twitterPreprocessingMode);	
}