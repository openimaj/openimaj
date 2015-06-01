package org.openimaj.math.statistics.normalisation;

import org.openimaj.math.util.DoubleArrayStatsUtils;
import org.openimaj.util.array.ArrayUtils;

/**
 * Subtract the mean of each example vector from itself and divide by the
 * standard deviation to normalise the vector such that it has unit variance. A
 * regularisation term can be optionally included in the divisor.
 * <p>
 * Only use if the data is stationary (i.e., the statistics for each data
 * dimension follow the same distribution).
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class PerExampleMeanCenterVar implements Normaliser {
	double eps = 10.0 / 255.0;

	/**
	 * Construct with the given variance regularisation term. Setting to zero
	 * disables the regulariser.
	 *
	 * @param eps
	 *            the variance normalisation regularizer (each dimension is
	 *            divided by sqrt(var + eps).
	 */
	public PerExampleMeanCenterVar(double eps) {
		this.eps = eps;
	}

	@Override
	public double[] normalise(double[] vector) {
		final double mean = DoubleArrayStatsUtils.mean(vector);
		final double var = DoubleArrayStatsUtils.var(vector);
		vector = ArrayUtils.subtract(vector, mean);
		vector = ArrayUtils.divide(vector, Math.sqrt(var + eps));

		return vector;
	}

	@Override
	public double[][] normalise(double[][] data) {
		final double[][] out = new double[data.length][];
		for (int c = 0; c < out.length; c++)
			out[c] = normalise(data[c]);
		return out;
	}
}
