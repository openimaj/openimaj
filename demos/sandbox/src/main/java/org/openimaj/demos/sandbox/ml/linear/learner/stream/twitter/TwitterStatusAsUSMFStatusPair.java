package org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter;

import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.function.Function;
import org.openimaj.util.pair.IndependentPair;

import twitter4j.Status;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TwitterStatusAsUSMFStatusPair implements Function<Status, IndependentPair<Status,USMFStatus>> {
	@Override
	public IndependentPair<Status,USMFStatus> apply(Status sstatus) {
		USMFStatus status = new USMFStatus();
		new GeneralJSONTweet4jStatus(sstatus).fillUSMF(status);
		return IndependentPair.pair(sstatus, status);
	}
}