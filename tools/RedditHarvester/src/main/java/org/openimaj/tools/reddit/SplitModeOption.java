package org.openimaj.tools.reddit;

import org.kohsuke.args4j.CmdLineOptionsProvider;

public enum SplitModeOption implements CmdLineOptionsProvider{
	/**
	 * 
	 */
	TIME{

		@Override
		public SplitMode getOptions() {
			return new TimeSplitMode();
		}
		
	},
	/**
	 * 
	 */
	COUNT{

		@Override
		public SplitMode getOptions() {
			return new CountSplitMode();
		}
		
	}
	;

	@Override
	public abstract SplitMode getOptions();

	
}
