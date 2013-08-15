package org.openimaj.ml.clustering.dbscan;


/**
 * Cluster based on connected components. Under the hood this is a {@link SimilarityDBSCAN}
 * with an epsilon of 0 and a min pts of 1.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class ContectedComponentSimilarityClusterer extends SimilarityDBSCAN{

	/**
	 * @param eps
	 * @param minPts
	 */
	public ContectedComponentSimilarityClusterer(double eps, int minPts) {
		super(0,1);
	}

}
