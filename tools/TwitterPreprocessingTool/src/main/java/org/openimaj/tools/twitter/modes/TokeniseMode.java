package org.openimaj.tools.twitter.modes;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.text.nlp.TweetTokeniser;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.twitter.TwitterStatus;

public class TokeniseMode implements TwitterPreprocessingMode {
	
	public final static String TOKENS = "tokens";
	
	public TokeniseMode() {}

	@Override
	public void process(TwitterStatus twitterStatus)  {
		TweetTokeniser tokeniser;
		try {
			tokeniser = new TweetTokeniser(twitterStatus.text);
			twitterStatus.addAnalysis(TOKENS , tokeniser.getTokens());
		} catch (Exception e) {
			twitterStatus.addAnalysis(TOKENS, null);
		}	
	}
	
	@Override
	public List<String> getAnalysisKeys(){
		ArrayList<String> analysis = new ArrayList<String>();
		analysis.add(TOKENS);
		return analysis;
	}
}
