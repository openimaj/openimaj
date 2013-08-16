package org.openimaj.demos.sandbox.ml.cluster.spectral;

import org.openimaj.logger.LoggerUtils;
import org.openimaj.ml.clustering.kmeans.DoubleKMeans;
import org.openimaj.ml.clustering.spectral.AbsoluteValueEigenChooser;
import org.openimaj.ml.clustering.spectral.GraphLaplacian;
import org.openimaj.ml.clustering.spectral.SpectralClusteringConf;

/**
 * Visualise a number of gaussians as historgrams. Control their means and
 * variances Visualise their top N eigen vectors
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class GaussianSpectralClusterKMeansVis extends GaussianSpectralClusterVis {

	public GaussianSpectralClusterKMeansVis() {
		super();
	}

	@Override
	protected SpectralClusteringConf<double[]> prepareConf() {
		final SpectralClusteringConf<double[]> conf = new SpectralClusteringConf<double[]>(
				DoubleKMeans.createExact(5, 1000),
				new GraphLaplacian.Normalised()
				);
		// conf.eigenChooser = new HardCodedEigenChooser(5);
		conf.eigenChooser = new AbsoluteValueEigenChooser(0.3, 0.1);
		return conf;
	}

	public static void main(String[] args) {
		LoggerUtils.prepareConsoleLogger();
		final GaussianSpectralClusterVis gscvis = new GaussianSpectralClusterKMeansVis();

	}

}
