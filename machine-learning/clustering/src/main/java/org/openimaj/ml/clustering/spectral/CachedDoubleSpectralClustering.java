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
	
	@Override
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
