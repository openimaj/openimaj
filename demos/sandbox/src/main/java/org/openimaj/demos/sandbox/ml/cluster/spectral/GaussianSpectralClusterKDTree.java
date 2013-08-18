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
