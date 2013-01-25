package org.openimaj.demos.sandbox.gmm;

public interface GaussianMixtureModelGenerator {
	class Generated{
		double[] point;
		int distribution;
		double[] responsibilities;
	}
	public Generated generate();
	public int dimentions();
}
