package org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter;

import org.openimaj.twitter.USMFStatus;

/**
 * The username of the {@link USMFStatus} instance is used as the
 * name
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class UserNameStrategy extends NameStrategy{

	@Override
	public String createName(USMFStatus stat) {
		return stat.user.name;
	}

}
