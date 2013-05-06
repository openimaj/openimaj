package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.tools.twitter.modes.filter.TwitterPreprocessingPredicate;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.function.Function;

/**
 * Given a {@link List} of {@link USMFStatus} instances, create a new {@link List}
 * containing only those which pass the {@link TwitterPredicateFunction} functions specified
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TwitterPredicateFunction implements Function<List<USMFStatus>, List<USMFStatus>>{

	
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
	public List<USMFStatus> apply(List<USMFStatus> in) {
		boolean passed = true;
		List<USMFStatus> ret = new ArrayList<USMFStatus>();
		for (USMFStatus usmfStatus : in) {
			for (TwitterPreprocessingPredicate filter : this.filters) {
				passed = filter.test(usmfStatus);
				if(!passed)break;
			}
			if(passed) ret.add(usmfStatus);
		}
		return ret;
	}

}
