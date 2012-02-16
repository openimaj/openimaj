package org.openimaj.tools.twitter.modes;

import org.openimaj.text.nlp.TweetTokeniser;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.twitter.TwitterStatus;

/**
 * Use the twokeniser to tokenise tweets
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class TokeniseMode implements TwitterPreprocessingMode {
	
	final static String TOKENS = "tokens";
	
	/**
	 * literally do nothing
	 */
	public TokeniseMode() {}

	@Override
	public void process(TwitterStatus twitterStatus)  {
		TweetTokeniser tokeniser;
		try {
			tokeniser = new TweetTokeniser(twitterStatus.text);
			twitterStatus.addAnalysis(TOKENS , tokeniser.getStringTokens());
		} catch (Exception e) {
			twitterStatus.addAnalysis(TOKENS, null);
		}	
	}
}
