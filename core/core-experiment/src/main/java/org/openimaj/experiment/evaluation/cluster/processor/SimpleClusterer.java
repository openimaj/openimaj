package org.openimaj.experiment.evaluation.cluster.processor;


/**
 * Something which wraps the functionality of producing clusters
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <DATA> 
 *
 */
public interface SimpleClusterer<DATA> {
	/**
	 * @param data 
	 * @return Given data items, cluster them by index
	 */
	public int[][] rawcluster(DATA data);
}
