package org.openimaj.tools.twitter.modes.output;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.tools.twitter.modes.TwitterOutputMode;
import org.openimaj.twitter.TwitterStatus;

public class SelectiveAnalysisOutputMode implements TwitterOutputMode{
	private List<String> selectiveAnalysis;

	public SelectiveAnalysisOutputMode() {
		this.selectiveAnalysis = new ArrayList<String>();
	}
	
	public SelectiveAnalysisOutputMode(List<String> selectiveAnalysis) {
		this.selectiveAnalysis = selectiveAnalysis;
	}

	@Override
	public void output(TwitterStatus twitterStatus, PrintWriter outputWriter) throws IOException {
		if(this.selectiveAnalysis.isEmpty()){
			twitterStatus.writeASCII(outputWriter);
		}
		else{
			twitterStatus.writeASCIIAnalysis(outputWriter,this.selectiveAnalysis);
		}
	}
	
}
