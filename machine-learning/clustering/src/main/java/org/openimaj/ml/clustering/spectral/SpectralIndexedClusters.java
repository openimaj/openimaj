package org.openimaj.ml.clustering.spectral;

import org.openimaj.ml.clustering.IndexClusters;
import org.openimaj.util.pair.IndependentPair;

/**
 * {@link IndexClusters} which also hold the eigenvector/value pairs which created them
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SpectralIndexedClusters extends IndexClusters {
	private IndependentPair<double[], double[][]> valvects;

	/**
	 * 
	 * @param c the underlying {@link IndexClusters}
	 * @param valvects the eigen values and vectors
	 */
	public SpectralIndexedClusters(IndexClusters c, IndependentPair<double[], double[][]>valvects) {
		this.clusters = c.clusters();
		this.nEntries = c.numEntries();
		this.valvects = valvects;
	}

	/**
	 * @return the eigen values
	 */
	public double[] eigenValues() {
		return valvects.firstObject();
	}

	public double[][] eigenVectors() {
		return valvects.getSecondObject();
	}
}
