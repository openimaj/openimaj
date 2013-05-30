package org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter;

import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.function.Function;

import twitter4j.Status;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TwitterStatusAsUSMFStatus implements Function<Status, USMFStatus> {
	@Override
	public USMFStatus apply(Status sstatus) {
		USMFStatus status = new USMFStatus();
		new GeneralJSONTweet4jStatus(sstatus).fillUSMF(status);
		return status;
	}
}