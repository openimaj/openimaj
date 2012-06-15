package org.openimaj.tools.twitter.modes.filter;

import java.util.List;
import java.util.Map;

import org.kohsuke.args4j.Option;
import org.openimaj.twitter.TwitterStatus;

/**
 * The grep functionality. Should only be used as a post filter most of the time
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class TokenFilter extends TwitterPreprocessingFilter {
	
	
	
	public boolean filter(TwitterStatus twitterStatus) {
		Map<String,List<String>> tokens = twitterStatus.getAnalysis("tokens");
		if(tokens==null) return false;
		List<String> alltoks = tokens.get("all");
		return false;
	}

}
