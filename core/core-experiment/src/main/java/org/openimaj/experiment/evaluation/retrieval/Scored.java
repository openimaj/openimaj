package org.openimaj.experiment.evaluation.retrieval;

/**
 * Interface for objects that have an associated score. Documents
 * retrieved by a {@link RetrievalEngine} might use this
 * interface to indicate their score to a {@link RetrievalAnalyser}. 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public interface Scored {
	/**
	 * @return the score of the object
	 */
	public double getScore();
}
