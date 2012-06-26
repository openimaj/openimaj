package org.openimaj.text.nlp.sentiment.type;

/**
 * Your sentiment was invalid
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class InvalidSentimentException extends Exception {

	/**
	 * @param message the sentiment message
	 */
	public InvalidSentimentException(String message) {
		super(message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2089389660634974588L;

}
