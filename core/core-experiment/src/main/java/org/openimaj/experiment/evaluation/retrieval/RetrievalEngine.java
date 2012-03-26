package org.openimaj.experiment.evaluation.retrieval;

import java.util.List;

import org.openimaj.experiment.dataset.Identifiable;

/**
 * Interface describing a retrieval engine
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <D> Type of document being retrieved
 * @param <Q> Type of query 
 */
public interface RetrievalEngine<D extends Identifiable, Q> {
	/**
	 * Search with the given query and return a ranked list of matching documents.
	 * @param query the query
	 * @return the results of the search
	 */
	List<D> search(Q query);
}
