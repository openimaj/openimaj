package org.openimaj.ml.clustering.spectral;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.openimaj.ml.clustering.DataClusterer;
import org.openimaj.ml.clustering.IndexClusters;
import org.openimaj.ml.clustering.SpatialClusterer;
import org.openimaj.ml.clustering.SpatialClusters;
import org.openimaj.util.pair.DoubleObjectPair;
import org.openimaj.util.pair.IndependentPair;

import ch.akuhn.matrix.Vector;
import ch.akuhn.matrix.Vector.Entry;
import ch.akuhn.matrix.eigenvalues.Eigenvalues;

/**
 * For a given set of {@link Eigenvalues} perform the stages of spectral clustering
 * which involve the selection of the best eigen values and the calling of an internal clustering
 * algorithm
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class PreparedSpectralClustering implements DataClusterer<Eigenvalues, SpectralIndexedClusters>{
	final static Logger logger = Logger.getLogger(PreparedSpectralClustering.class);
	private SpectralClusteringConf<double[]> conf;

	/**
	 * @param conf
	 */
	public PreparedSpectralClustering(SpectralClusteringConf<double[]> conf) {
		this.conf = conf;
	}

	@Override
	public int[][] performClustering(Eigenvalues data) {
		return cluster(data).clusters();
	}

	@Override
	public SpectralIndexedClusters cluster(Eigenvalues eig) {
		// Also normalise each row
		IndependentPair<double[], double[][]> lowestCols = bestCols(eig);
		// Using the eigenspace, cluster
		return eigenspaceCluster(lowestCols);
	}
	
	protected SpectralIndexedClusters eigenspaceCluster(IndependentPair<double[], double[][]> lowestCols) {
		SpatialClusterer<? extends SpatialClusters<double[]>, double[]> clusterer = conf.internal.apply(lowestCols);
		// Cluster the rows with the internal spatial clusterer
		SpatialClusters<double[]> cluster = clusterer.cluster(lowestCols.getSecondObject());
		// if the clusters contain the cluster indexes of the training examples use those
		if(cluster instanceof IndexClusters){
			IndexClusters clusters = new IndexClusters(((IndexClusters)cluster).clusters());
			logger.debug(clusters);
			return new SpectralIndexedClusters(clusters, lowestCols);
		}
		// Otherwise attempt to assign values to clusters
		int[] clustered = cluster.defaultHardAssigner().assign(lowestCols.getSecondObject());
		// done!
		return new SpectralIndexedClusters(new IndexClusters(clustered),lowestCols);
	}
	
	protected IndependentPair<double[], double[][]> bestCols(final Eigenvalues eig) {
		
		
		int eigenVectorSelect = conf.eigenChooser.nEigenVectors(this.conf.laplacian.eigenIterator(eig), eig.getN());
		logger.debug("Selected dimensions: " + eigenVectorSelect);


		int nrows = eig.vector[0].size();
		double[][] ret = new double[nrows][eigenVectorSelect];
		double[] retSum = new double[nrows];
		double[] eigvals = new double[eigenVectorSelect];
		int col = 0;
		// Calculate U matrix (containing n smallests eigen valued columns)
		for (Iterator<DoubleObjectPair<Vector>> iterator = this.conf.laplacian.eigenIterator(eig); iterator.hasNext();) {
			DoubleObjectPair<Vector> v = iterator.next();
			eigvals[col] = v.first;
			
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

		return IndependentPair.pair(eigvals, ret);
	}

}
