package org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter;

import org.apache.log4j.Logger;
import org.openimaj.tools.twitter.modes.preprocessing.CountryCodeMode;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.twitter.USMFStatus;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class CountryCodeNameStrategy extends NameStrategy {
	Logger logger = Logger.getLogger(CountryCodeNameStrategy.class);
	CountryCodeMode mode = new CountryCodeMode();
	@Override
	public String createName(USMFStatus stat) {
		try {
			return TwitterPreprocessingMode.results(stat, mode);
		} catch (Exception e) {
			return null;
		}
	}

}
