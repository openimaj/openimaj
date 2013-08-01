package org.openimaj.ml.clustering.spectral;



import java.util.Iterator;

import org.apache.log4j.Logger;
import org.openimaj.ml.clustering.SimilarityClusterer;
import org.openimaj.ml.clustering.SpatialClusters;
import org.openimaj.ml.clustering.IndexClusters;
import org.openimaj.util.pair.DoubleObjectPair;

import ch.akuhn.matrix.SparseMatrix;
import ch.akuhn.matrix.Vector;
import ch.akuhn.matrix.Vector.Entry;
import ch.akuhn.matrix.eigenvalues.Eigenvalues;

/**
 * Built from a mixture of this tutorial:
 * 	- http://www.kyb.mpg.de/fileadmin/user_upload/files/publications/attachments/Luxburg07_tutorial_4488%5B0%5D.pdf
 * And this implementation:
 *  - https://github.com/peterklipfel/AutoponicsVision/blob/master/SpectralClustering.java
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class DoubleSpectralClustering implements SimilarityClusterer<IndexClusters>{
	final static Logger logger = Logger.getLogger(DoubleSpectralClustering.class);
	private SpectralClusteringConf<double[]> conf;

	/**
	 * @param conf
	 * cluster the eigen vectors
	 */
	public DoubleSpectralClustering(SpectralClusteringConf<double[]> conf) {
		this.conf = conf;
	}
	
	@Override
	public IndexClusters clusterSimilarity(SparseMatrix sim) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public IndexClusters cluster(SparseMatrix data) {
		// Get the laplacian, solve the eigen problem and choose the best 
		double[][] lowestCols = spectralCluster(data);
		// Using the eigenspace, cluster
		return eigenspaceCluster(lowestCols);
	}

	protected IndexClusters eigenspaceCluster(double[][] lowestCols) {
		// Cluster the rows with the internal spatial clusterer
		SpatialClusters<double[]> cluster = conf.internal.cluster(lowestCols);
		// if the clusters contain the cluster indexes of the training examples use those
		if(cluster instanceof IndexClusters){
			IndexClusters clusters = new IndexClusters(((IndexClusters)cluster).clusters());
			logger.debug(clusters);
			return clusters;
		}
		// Otherwise attempt to assign values to clusters
		int[] clustered = cluster.defaultHardAssigner().assign(lowestCols);
		// done!
		return new IndexClusters(clustered);
	}

	protected double[][] spectralCluster(SparseMatrix data) {
		// Compute the laplacian of the graph
		final SparseMatrix laplacian = laplacian(data);
		// Calculate the eigvectors
		// Use the lowest eigen valued cols as the features, each row is a data item in the reduced feature space
		// Also normalise each row
		double[][] lowestCols = bestCols(laplacian);
		return lowestCols;
	}

	protected SparseMatrix laplacian(SparseMatrix data) {
		return conf.laplacian.laplacian(data);
	}
	
	protected double[][] bestCols(final SparseMatrix laplacian) {
		
		Eigenvalues eig = conf.eigenChooser.prepare(laplacian, conf.laplacian.direction());
		eig.run();
		int eigenVectorSelect = conf.eigenChooser.nEigenVectors(this.conf.laplacian.eigenIterator(eig),laplacian.columnCount());
		logger.debug("Selected dimensions: " + eigenVectorSelect);


		int nrows = eig.vector[0].size();
		double[][] ret = new double[nrows][eigenVectorSelect];
		double[] retSum = new double[nrows];
		int col = 0;
		// Calculate U matrix (containing n smallests eigen valued columns)
		for (Iterator<DoubleObjectPair<Vector>> iterator = this.conf.laplacian.eigenIterator(eig); iterator.hasNext();) {
			DoubleObjectPair<Vector> v = iterator.next();
			for (Entry d : v.second.entries()) {
				double elColI = d.value;
				ret[d.index][col] = elColI;
				retSum[d.index] += elColI * elColI;
			}
			col++;
			if(col == eigenVectorSelect) break;
		}

		// normalise rows
		for (int i = 0; i < ret.length; i++) {
			double[] row = ret[i];
			for (int j = 0; j < row.length; j++) {
				row[j] /= Math.sqrt(retSum[i]);
			}
		}

		return ret;
	}

	@Override
	public int[][] rawcluster(SparseMatrix data) {
		return this.cluster(data).clusters();
	}

	

}
