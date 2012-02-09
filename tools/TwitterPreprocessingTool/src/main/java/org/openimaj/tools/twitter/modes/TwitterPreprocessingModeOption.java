package org.openimaj.tools.twitter.modes;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;

public enum TwitterPreprocessingModeOption implements CmdLineOptionsProvider {
	TOKENISE{
		@Override
		public TwitterPreprocessingMode createMode() {
			return new TokeniseMode();
		}
	};

	
	
	@Override
	public Object getOptions() {return this;}

	public abstract TwitterPreprocessingMode createMode();
	
}

