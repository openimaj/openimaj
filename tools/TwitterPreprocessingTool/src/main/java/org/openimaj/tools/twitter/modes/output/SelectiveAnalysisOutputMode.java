package org.openimaj.tools.twitter.modes.output;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.twitter.TwitterStatus;

/**
 * An ouput mode which alters the tweets being outputted
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class SelectiveAnalysisOutputMode implements TwitterOutputMode{
	private List<String> selectiveAnalysis;
	private String delim = null;

	/**
	 * Non selective, output everything 
	 */
	public SelectiveAnalysisOutputMode() {
		this.selectiveAnalysis = new ArrayList<String>();
	}
	
	/**
	 * Only output the analysis keys given, dump the rest of the tweet. If the selectiveAnalysis is empty,
	 * the whole tweet is outputted.
	 * 
	 * @param selectiveAnalysis
	 */
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
		if(delim != null){
			outputWriter.print(this.delim);
		}
	}

	@Override
	public void deliminate(String string) {
		this.delim  = string;
	}
	
}
