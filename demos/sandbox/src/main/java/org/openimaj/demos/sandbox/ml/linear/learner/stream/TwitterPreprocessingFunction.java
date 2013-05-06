package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.util.List;

import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.function.Function;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TwitterPreprocessingFunction implements  Function<List<USMFStatus>,List<USMFStatus>> {


	private TwitterPreprocessingMode<?>[] options;

	/**
	 * @param options
	 */
	public TwitterPreprocessingFunction(TwitterPreprocessingMode<?> ... options) {
		this.options = options;
	}

	@Override
	public List<USMFStatus> apply(List<USMFStatus> in) {
		for (USMFStatus usmfStatus : in) {
			for (TwitterPreprocessingMode<?> opt: this.options) {
				try {
					TwitterPreprocessingMode.results(usmfStatus, opt);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return in;
	}


}
