package org.openimaj.text.nlp.sentiment.type;

/**
 * A sentiment which can be collapsed into a {@link BipolarSentiment} which is either positive or negative
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface BipolarSentimentProvider {
	/**
	 * @return a collapsed version of the sentiment that is either positive, negaitve or neutral
	 */
	public BipolarSentiment bipolar();
	
	/**
	 * @param deltaThresh A a proportional threshold
	 * @return a collapsed version of the sentiment that is either positive, negaitve or neutral
	 */
	public BipolarSentiment bipolar(double deltaThresh);
}
