package org.openimaj.tools.twitter.modes.preprocessing;

import org.openimaj.twitter.TwitterStatus;

/**
 * A processing mode that is able to process a tweet and also typed on the data which it 
 * analyses from the tweet (so it can return this data if required)
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 * @param <T> The type of the analysis result
 *
 */
public interface TwitterPreprocessingMode<T> {

	/**
	 * Alters the twitter status in place with the analysis that is required to be performed
	 * @param twitterStatus
	 * @return for conveniance also returns the analysis
	 */
	public T process(TwitterStatus twitterStatus);
}
