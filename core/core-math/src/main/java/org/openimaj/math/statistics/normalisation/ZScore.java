package org.openimaj.math.statistics.normalisation;

/**
 * z-score normalisation (standardisation). Upon training, the mean and variance
 * of each dimension is computed; normalisation works by subtracting the mean
 * and dividing by the standard deviation.
 * <p>
 * This implementation includes an optional regularisation parameter that is
 * added to the variance before the division.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ZScore implements TrainableNormaliser, Denormaliser {
	double[] mean;
	double[] sigma;
	double eps = 0;

	/**
	 * Construct without regularisation.
	 */
	public ZScore() {
	}

	/**
	 * Construct with regularisation.
	 *
	 * @param eps
	 *            the variance normalisation regulariser (each dimension is
	 *            divided by sqrt(var + eps).
	 */
	public ZScore(double eps) {
	}

	@Override
	public void train(double[][] data) {
		mean = new double[data[0].length];
		sigma = new double[data[0].length];

		for (int r = 0; r < data.length; r++)
			for (int c = 0; c < data[0].length; c++)
				mean[c] += data[r][c];

		for (int c = 0; c < data[0].length; c++)
			mean[c] /= data.length;

		for (int r = 0; r < data.length; r++) {
			for (int c = 0; c < data[0].length; c++) {
				final double delta = (data[r][c] - mean[c]);
				sigma[c] += delta * delta;
			}
		}

		for (int c = 0; c < data[0].length; c++)
			sigma[c] = Math.sqrt(eps + (sigma[c] / (data.length - 1)));
	}

	@Override
	public double[] normalise(double[] vector) {
		final double[] out = new double[vector.length];
		for (int c = 0; c < out.length; c++)
			out[c] = (vector[c] - mean[c]) / sigma[c];
		return out;
	}

	@Override
	public double[][] normalise(double[][] data) {
		final double[][] out = new double[data.length][];
		for (int c = 0; c < out.length; c++)
			out[c] = normalise(data[c]);
		return out;
	}

	@Override
	public double[] denormalise(double[] vector) {
		final double[] out = new double[vector.length];
		for (int c = 0; c < out.length; c++)
			out[c] = sigma[c] * vector[c] + mean[c];
		return out;
	}

	@Override
	public double[][] denormalise(double[][] data) {
		final double[][] out = new double[data.length][];
		for (int c = 0; c < out.length; c++)
			out[c] = denormalise(data[c]);
		return out;
	}
}
