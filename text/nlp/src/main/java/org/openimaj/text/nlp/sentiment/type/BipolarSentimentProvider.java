package org.openimaj.text.nlp.sentiment.type;

/**
 * A sentiment which can be collapsed into a {@link BipolarSentiment} which is either positive or negative
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public interface BipolarSentimentProvider {
	/**
	 * @return a collapsed version of the sentiment that is either positive, negaitve or neutral
	 */
	public BipolarSentiment bipolar();
}
