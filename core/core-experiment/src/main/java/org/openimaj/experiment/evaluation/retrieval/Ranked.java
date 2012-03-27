package org.openimaj.experiment.evaluation.retrieval;

/**
 * Interface for objects that have a ranked position. Documents
 * retrieved by a {@link RetrievalEngine} might use this
 * interface to indicate their ranked position to a 
 * {@link RetrievalAnalyser} rather than using their position
 * in the results list. 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public interface Ranked {
	/**
	 * @return the rank of the object
	 */
	public int getRank();
}
