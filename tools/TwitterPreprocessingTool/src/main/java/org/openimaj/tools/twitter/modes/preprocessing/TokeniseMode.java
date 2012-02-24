package org.openimaj.tools.twitter.modes.preprocessing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.text.nlp.TweetTokeniser;
import org.openimaj.twitter.TwitterStatus;

/**
 * Use the twokeniser to tokenise tweets
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class TokeniseMode implements TwitterPreprocessingMode<Map<String,List<String>>> {
	
	final static String TOKENS = "tokens";
	public static final String TOKENS_UNPROTECTED = "unprotected";
	public static final String TOKENS_PROTECTED = "protected";
	public static final String TOKENS_ALL = "all";
	
	/**
	 * literally do nothing
	 */
	public TokeniseMode() {}

	@Override
	public Map<String,List<String>> process(TwitterStatus twitterStatus)  {
		TweetTokeniser tokeniser;
		Map<String,List<String>> tokens = new HashMap<String,List<String>>();
		try {
			tokeniser = new TweetTokeniser(twitterStatus.text);
			tokens.put(TOKENS_ALL, tokeniser.getStringTokens());
			tokens.put(TOKENS_PROTECTED, tokeniser.getProtectedStringTokens());
			tokens.put(TOKENS_UNPROTECTED, tokeniser.getUnprotectedStringTokens());
			twitterStatus.addAnalysis(TOKENS,tokens);
		} catch (Exception e) {
		}	
		twitterStatus.addAnalysis(TOKENS,tokens);
		return tokens;
	}
}
