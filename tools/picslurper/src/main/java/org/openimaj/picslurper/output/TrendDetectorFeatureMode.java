package org.openimaj.picslurper.output;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.openimaj.picslurper.client.LocalColourFeatureMode;
import org.openimaj.picslurper.client.SIFTTrendFeatureMode;
import org.openimaj.picslurper.client.TrendDetectorFeatureExtractor;

public enum TrendDetectorFeatureMode implements CmdLineOptionsProvider{
	SIFT {
		@Override
		public TrendDetectorFeatureExtractor getOptions() {
			return new SIFTTrendFeatureMode();
		}
	},
	LOCAL_COLOUR {
		@Override
		public TrendDetectorFeatureExtractor getOptions() {
			return new LocalColourFeatureMode();
		}
	}
	;

	@Override
	public abstract TrendDetectorFeatureExtractor getOptions() ;

}
