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
package org.openimaj.demos.sandbox.ml.cluster.spectral;

import org.openimaj.logger.LoggerUtils;
import org.openimaj.ml.clustering.kdtree.DoubleKDTreeClusterer;
import org.openimaj.ml.clustering.kmeans.DoubleKMeans;
import org.openimaj.ml.clustering.spectral.AbsoluteValueEigenChooser;
import org.openimaj.ml.clustering.spectral.GraphLaplacian;
import org.openimaj.ml.clustering.spectral.SpectralClusteringConf;

/**
 * Visualise a number of gaussians as historgrams.
 * Control their means and variances
 * Visualise their top N eigen vectors
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class GaussianSpectralClusterKDTree extends GaussianSpectralClusterVis{
	
	/**
	 * 
	 */
	public GaussianSpectralClusterKDTree() {
		super();
	}
	
	protected SpectralClusteringConf<double[]> prepareConf() {
		SpectralClusteringConf<double[]> conf = new SpectralClusteringConf<double[]>(
				new DoubleKDTreeClusterer(0.01,1,4)
		);
//		conf.eigenChooser = new HardCodedEigenChooser(5);
		conf.eigenChooser = new AbsoluteValueEigenChooser(0.5, 0.1);
//		conf.laplacian = new GraphLaplacian.Unnormalised();
		return conf;
	}
	
	public static void main(String[] args) {
		LoggerUtils.prepareConsoleLogger();
		GaussianSpectralClusterVis gscvis = new GaussianSpectralClusterKDTree();
		
	}

}
