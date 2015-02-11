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
	protected SpectralClusteringConf<double[]> conf;

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
		Eigenvalues eig = conf.eigenChooser.prepare(laplacian);
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
