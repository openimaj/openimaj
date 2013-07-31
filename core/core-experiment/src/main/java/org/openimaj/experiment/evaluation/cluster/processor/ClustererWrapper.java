package org.openimaj.experiment.evaluation.cluster.processor;


/**
 * Something which wraps the functionality of producing clusters
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface ClustererWrapper {
	/**
	 * @return Given a list of data items, cluster them by index
	 */
	public int[][] cluster();
}
