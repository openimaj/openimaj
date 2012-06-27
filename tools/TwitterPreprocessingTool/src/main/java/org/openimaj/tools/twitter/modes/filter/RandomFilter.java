package org.openimaj.tools.twitter.modes.filter;

import java.util.Random;

import org.kohsuke.args4j.Option;
import org.openimaj.twitter.USMFStatus;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class RandomFilter extends TwitterPreprocessingFilter {

	@Option(name="--random-filter-chance", aliases="-rfc", required=false, usage="The chance that a tweet will be emitted")
	float chance = 0.01f;
	
	Random r = new Random();
	@Override
	public boolean filter(USMFStatus twitterStatus) {
		return r.nextDouble() < chance;
	}

}
