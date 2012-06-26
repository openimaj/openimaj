package org.openimaj.text.nlp.sentiment.type;

/**
 * Provides a {@link DiscreteCountBipolarSentiment} based on the (presumebly more complex) sentiment 
 * held
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface DiscreteCountBipolarSentimentProvider {
	/**
	 * @return a new DiscreteCountBipolarSentiment instance based on the sentiments held
	 */
	public DiscreteCountBipolarSentiment countBipolarSentiment();
}
