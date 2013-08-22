package org.openimaj.ml.clustering.spectral;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.openimaj.io.IOUtils;

import ch.akuhn.matrix.SparseMatrix;
import ch.akuhn.matrix.eigenvalues.Eigenvalues;

/**
 * {@link DoubleSpectralClustering} extention which knows how to write and read its eigenvectors to disk
 * and therefore not regenerate them when calling the underlying {@link PreparedSpectralClustering} 
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class CachedDoubleSpectralClustering extends DoubleSpectralClustering{
	private final static Logger logger = Logger.getLogger(CachedDoubleSpectralClustering.class);
	private File cache;

	/**
	 * @param cache location to cache the eigenvectors
	 * @param conf
	 * cluster the eigen vectors
	 */
	public CachedDoubleSpectralClustering(File cache, SpectralClusteringConf<double[]> conf) {
		super(conf);
		this.cache = cache;
	}
	
	protected Eigenvalues spectralCluster(SparseMatrix data) {
		Eigenvalues eig = null;
		if(cache.exists()){
			logger.debug("Loading eigenvectors from cache");
			try {
				eig = IOUtils.readFromFile(cache);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		else{			
			// Compute the laplacian of the graph
			logger.debug("Cache empty, recreating eigenvectors");
			final SparseMatrix laplacian = laplacian(data);
			eig = laplacianEigenVectors(laplacian);
			try {
				logger.debug("Writing eigenvectors to cache");
				IOUtils.writeToFile(eig, cache);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		return eig;
	}
	
}
