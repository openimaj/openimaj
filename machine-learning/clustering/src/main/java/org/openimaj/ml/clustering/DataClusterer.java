package org.openimaj.ml.clustering;

import org.openimaj.experiment.evaluation.cluster.processor.Clusterer;

/**
 * Clusterers can extract clusters from data types and return 
 * the data in a clustered form
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <DATA> the data which can be clustered
 * @param <CLUSTER> 
 */
public interface DataClusterer<DATA, CLUSTER extends IndexClusters> extends Clusterer<DATA>{
	/**
	 * @param data the data to be clustered
	 * @return the clusters produced
	 */
	public CLUSTER cluster(DATA data);
}
