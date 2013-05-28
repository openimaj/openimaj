package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.function.Function;
import org.openimaj.util.pair.IndependentPair;

import twitter4j.Status;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TwitterPreprocessingFunctionPair implements  Function<IndependentPair<Status,USMFStatus>,IndependentPair<Status,USMFStatus>> {


	private TwitterPreprocessingMode<?>[] options;

	/**
	 * @param options
	 */
	public TwitterPreprocessingFunctionPair(TwitterPreprocessingMode<?> ... options) {
		this.options = options;
	}

	@Override
	public IndependentPair<Status,USMFStatus> apply(IndependentPair<Status,USMFStatus> in) {
		for (TwitterPreprocessingMode<?> opt: this.options) {
			try {
				TwitterPreprocessingMode.results(in.getSecondObject(), opt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return in;
	}


}
