package org.openimaj.ml.clustering.spectral;

import org.openimaj.ml.clustering.SpatialClusterer;
import org.openimaj.ml.clustering.SpatialClusters;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * Provides similar configuration to {@link SpectralClusteringConf} along with a lambda 
 * which acts as the co-regularisation parameter
 *
 * @param <DATATYPE>
 */
public class MultiviewSpectralClusteringConf<DATATYPE> extends SpectralClusteringConf<DATATYPE>{

	/**
	 * regularisation parameter
	 */
	public double lambda;
	
	/**
	 * when to stop iterating
	 */
	public StoppingCondition stop;

	/**
	 * @param lambda coregularisaton parameter
	 * @param internal
	 * @param laplacian
	 */
	public MultiviewSpectralClusteringConf(double lambda, SpatialClusterer<? extends SpatialClusters<DATATYPE>, DATATYPE> internal,GraphLaplacian laplacian) {
		super(internal, laplacian);
		this.lambda = lambda;
	}

	/**
	 * @param lambda coregularisaton parameter
	 * @param internal
	 * @param eigK
	 */
	public MultiviewSpectralClusteringConf(double lambda, SpatialClusterer<? extends SpatialClusters<DATATYPE>, DATATYPE> internal,int eigK) {
		super(internal, eigK);
		this.lambda = lambda;
	}

	/**
	 * @param lambda coregularisaton parameter
	 * @param internal
	 */
	public MultiviewSpectralClusteringConf(double lambda, SpatialClusterer<? extends SpatialClusters<DATATYPE>, DATATYPE> internal) {
		super(internal);
		this.lambda = lambda;
	}


}
