package org.openimaj.tools.twitter.modes.filter;

import java.util.Random;

import org.openimaj.twitter.TwitterStatus;

/**
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class RandomFilter extends TwitterPreprocessingFilter {

	float chance = 0.1f;
	
	Random r = new Random();
	@Override
	public boolean filter(TwitterStatus twitterStatus) {
		return r.nextDouble() < chance;
	}

}
