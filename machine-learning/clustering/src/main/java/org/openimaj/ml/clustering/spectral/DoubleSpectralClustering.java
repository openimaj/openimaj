package org.openimaj.ml.clustering.spectral;
import org.apache.log4j.Logger;
import org.openimaj.ml.clustering.SimilarityClusterer;

import ch.akuhn.matrix.SparseMatrix;
import ch.akuhn.matrix.eigenvalues.Eigenvalues;

/**
 * Built from a mixture of this tutorial:
 * 	- http://www.kyb.mpg.de/fileadmin/user_upload/files/publications/attachments/Luxburg07_tutorial_4488%5B0%5D.pdf
 * And this implementation:
 *  - https://github.com/peterklipfel/AutoponicsVision/blob/master/SpectralClustering.java
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class DoubleSpectralClustering implements SimilarityClusterer<SpectralIndexedClusters>{
	final static Logger logger = Logger.getLogger(DoubleSpectralClustering.class);
	private SpectralClusteringConf<double[]> conf;

	/**
	 * @param conf
	 * cluster the eigen vectors
	 */
	public DoubleSpectralClustering(SpectralClusteringConf<double[]> conf) {
		this.conf = conf;
	}
	
	protected DoubleSpectralClustering() {
	}
	
	@Override
	public SpectralIndexedClusters clusterSimilarity(SparseMatrix sim) {
		return cluster(sim);
	}
	
	@Override
	public SpectralIndexedClusters cluster(SparseMatrix data) {
		// Get the laplacian, solve the eigen problem and choose the best 
		// Use the lowest eigen valued cols as the features, each row is a data item in the reduced feature space
		Eigenvalues eig = spectralCluster(data);
		PreparedSpectralClustering prep = new PreparedSpectralClustering(conf);
		return prep.cluster(eig);
	}

	

	protected Eigenvalues spectralCluster(SparseMatrix data) {
		// Compute the laplacian of the graph
		final SparseMatrix laplacian = laplacian(data);
		Eigenvalues eig = laplacianEigenVectors(laplacian);
		
		return eig;
	}

	protected Eigenvalues laplacianEigenVectors(final SparseMatrix laplacian) {
		// Calculate the eigvectors
		Eigenvalues eig = conf.eigenChooser.prepare(laplacian, conf.laplacian.direction());
		eig.run();
		return eig;
	}

	protected SparseMatrix laplacian(SparseMatrix data) {
		return conf.laplacian.laplacian(data);
	}

	@Override
	public int[][] performClustering(SparseMatrix data) {
		return this.cluster(data).clusters();
	}
	
	@Override
	public String toString() {
		return String.format("%s: {Laplacian: %s, EigenChooser: %s, SpatialClusterer: %s}",simpleName(this),simpleName(conf.laplacian),simpleName(conf.eigenChooser),conf.internal);
	}

	private String simpleName(Object o) {
		return o.getClass().getSimpleName();
	}
}
