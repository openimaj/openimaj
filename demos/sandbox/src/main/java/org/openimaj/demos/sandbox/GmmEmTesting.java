package org.openimaj.demos.sandbox;

import java.util.Random;

import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.math.statistics.distribution.MixtureOfGaussians;
import org.openimaj.ml.gmm.GaussianMixtureModelEM;
import org.openimaj.ml.gmm.GaussianMixtureModelEM.CovarianceType;

public class GmmEmTesting {
	public static void main(String[] args) {
		final Random rng = new Random();
		final double[][] data = new double[1000][4];
		for (int j = 0; j < data.length / 2; j++) {
			for (int i = 0; i < data[0].length; i++) {
				data[j][i] = rng.nextGaussian() * (i + 1);
			}
		}

		for (int j = data.length / 2; j < data.length; j++) {
			for (int i = 0; i < data[0].length; i++) {
				data[j][i] = 10 + rng.nextGaussian() * (i + 1);
			}
		}

		final GaussianMixtureModelEM gmmem = new GaussianMixtureModelEM(2, CovarianceType.Spherical);
		final MixtureOfGaussians model = gmmem.estimate(data);

		System.out.println(MatrixUtils.toString(model.gaussians[0].getCovariance()));
		System.out.println();
		System.out.println(MatrixUtils.toString(model.gaussians[1].getCovariance()));
	}
}
