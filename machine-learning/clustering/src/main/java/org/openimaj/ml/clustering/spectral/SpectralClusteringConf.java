package org.openimaj.ml.clustering.spectral;

import org.openimaj.ml.clustering.SpatialClusterer;
import org.openimaj.ml.clustering.SpatialClusters;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <DATATYPE>
 *
 */
public class SpectralClusteringConf<DATATYPE>{

	/**
	 * The internal clusterer
	 */
	public SpatialClusterer<? extends SpatialClusters<DATATYPE>,DATATYPE> internal;

	/**
	 * The graph laplacian creator
	 */
	GraphLaplacian laplacian;

	/**
	 * The method used to select the number of eigen vectors from the lower valued eigenvalues
	 */
	public EigenChooser eigenChooser;

	/**
	 * @param internal the internal clusterer
	 * @param eigK the value handed to {@link HardCodedEigenChooser}
	 *
	 */
	public SpectralClusteringConf(SpatialClusterer<? extends SpatialClusters<DATATYPE>,DATATYPE> internal, int eigK) {
		this.internal = internal;
		this.laplacian = new GraphLaplacian.Symmetric();
		this.eigenChooser = new HardCodedEigenChooser(eigK);

	}

	/**
	 * The underlying {@link EigenChooser} is set to an {@link AutoSelectingEigenChooser} which
	 * looks for a 100x gap between eigen vectors to select number of clusters. It also insists upon
	 * a maximum of 0.1 * number of data items (so 10 items per cluster)
	 *
	 * @param internal the internal clusterer
	 *
	 */
	public SpectralClusteringConf(SpatialClusterer<? extends SpatialClusters<DATATYPE>,DATATYPE> internal) {
		this.internal = internal;
		this.laplacian = new GraphLaplacian.Symmetric();
		this.eigenChooser = new AutoSelectingEigenChooser(100,0.1);

	}

	/**
	 * The underlying {@link EigenChooser} is set to an {@link AutoSelectingEigenChooser} which
	 * looks for a 100x gap between eigen vectors to select number of clusters. It also insists upon
	 * a maximum of 0.1 * number of data items (so 10 items per cluster)
	 *
	 * @param internal the internal clusterer
	 * @param laplacian the graph laplacian
	 *
	 */
	public SpectralClusteringConf(SpatialClusterer<? extends SpatialClusters<DATATYPE>,DATATYPE> internal, GraphLaplacian laplacian) {
		this.internal = internal;
		this.laplacian = laplacian;
		this.eigenChooser = new AutoSelectingEigenChooser(100,0.1);

	}
}