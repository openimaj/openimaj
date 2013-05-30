package org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter;

import java.util.List;

import org.openimaj.tools.twitter.modes.filter.TwitterPreprocessingPredicate;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.function.Predicate;

/**
 * Given a {@link List} of {@link USMFStatus} instances, create a new {@link List}
 * containing only those which pass the {@link TwitterPredicateFunction} functions specified
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TwitterPredicateFunction implements Predicate<USMFStatus>{


	private TwitterPreprocessingPredicate[] filters;
	/**
	 * @param filters
	 */
	public TwitterPredicateFunction(TwitterPreprocessingPredicate...filters) {
		this.filters = filters;
		for (TwitterPreprocessingPredicate filter : filters) {
			filter.validate();
		}
	}

	@Override
	public boolean test(USMFStatus object) {
		boolean passed = true;
		for (TwitterPreprocessingPredicate filter : this.filters) {
			passed = filter.test(object);
			if(!passed)break;
		}
		return passed;
	}

}
