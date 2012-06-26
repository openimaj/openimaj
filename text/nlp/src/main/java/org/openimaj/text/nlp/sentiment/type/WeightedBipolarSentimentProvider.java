package org.openimaj.text.nlp.sentiment.type;

/**
 * A sentiment which can be collapsed into a {@link WeightedBipolarSentiment} 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface WeightedBipolarSentimentProvider {
	/**
	 * @return a collapsed version of the sentiment that is positive, negaitve or neutral all to some degree
	 */
	public WeightedBipolarSentiment weightedBipolar();
}
