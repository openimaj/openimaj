package org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter;

import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.function.Function;

/**
 * Given a {@link USMFStatus} instance, construct a String
 * which is used as the "name" of the "user" which created this
 * social media artifact
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class NameStrategy implements Function<USMFStatus,String>{
	/**
	 * @param stat
	 * @return some string
	 */
	public abstract String createName(USMFStatus stat);
	@Override
	public String apply(USMFStatus in) {
		return createName(in);
	}
}
