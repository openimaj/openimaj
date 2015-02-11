/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
 * For a given set of {@link Eigenvalues} perform the stages of spectral
 * clustering which involve the selection of the best eigen values and the
 * calling of an internal clustering algorithm
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class PreparedSpectralClustering implements DataClusterer<Eigenvalues, SpectralIndexedClusters> {
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
		final IndependentPair<double[], double[][]> lowestCols = bestCols(eig);
		// Using the eigenspace, cluster
		return eigenspaceCluster(lowestCols);
	}

	protected SpectralIndexedClusters eigenspaceCluster(IndependentPair<double[], double[][]> lowestCols) {
		final SpatialClusterer<? extends SpatialClusters<double[]>, double[]> clusterer = conf.internal.apply(lowestCols);
		// Cluster the rows with the internal spatial clusterer
		final SpatialClusters<double[]> cluster = clusterer.cluster(lowestCols.getSecondObject());
		// if the clusters contain the cluster indexes of the training examples
		// use those
		if (cluster instanceof IndexClusters) {
			final IndexClusters clusters = new IndexClusters(((IndexClusters) cluster).clusters());
			// logger.debug(clusters);
			return new SpectralIndexedClusters(clusters, lowestCols);
		}
		// Otherwise attempt to assign values to clusters
		final int[] clustered = cluster.defaultHardAssigner().assign(lowestCols.getSecondObject());
		// done!
		return new SpectralIndexedClusters(new IndexClusters(clustered), lowestCols);
	}

	protected IndependentPair<double[], double[][]> bestCols(final Eigenvalues eig) {

		int eigenVectorSelect = conf.eigenChooser.nEigenVectors(this.conf.laplacian.eigenIterator(eig), eig.getN());
		final int eigenVectorSkip = this.conf.skipEigenVectors;
		logger.debug("Selected dimensions: " + eigenVectorSelect);
		logger.debug("Skipping dimesions: " + eigenVectorSkip);
		eigenVectorSelect -= eigenVectorSkip;

		final int nrows = eig.vector[0].size();
		final double[][] ret = new double[nrows][eigenVectorSelect];
		final double[] retSum = new double[nrows];
		final double[] eigvals = new double[eigenVectorSelect];
		final Iterator<DoubleObjectPair<Vector>> iterator = this.conf.laplacian.eigenIterator(eig);
		// Skip a few at the beggining
		for (int i = 0; i < eigenVectorSkip; i++)
			iterator.next();
		int col = 0;
		// Calculate U matrix (containing n smallests eigen valued columns)
		for (; iterator.hasNext();) {
			final DoubleObjectPair<Vector> v = iterator.next();
			eigvals[col] = v.first;

			for (final Entry d : v.second.entries()) {
				double elColI = d.value;
				if (conf.eigenValueScale) {
					elColI *= Math.sqrt(v.first);
				}
				ret[d.index][col] = elColI;
				retSum[d.index] += elColI * elColI;
			}
			col++;
			if (col == eigenVectorSelect)
				break;
		}

		if (!conf.eigenValueScale) {
			// normalise rows
			for (int i = 0; i < ret.length; i++) {
				final double[] row = ret[i];
				for (int j = 0; j < row.length; j++) {
					row[j] /= Math.sqrt(retSum[i]);
				}
			}
		}

		return IndependentPair.pair(eigvals, ret);
	}

}
