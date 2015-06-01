package org.openimaj.math.statistics.normalisation;

import org.openimaj.math.util.DoubleArrayStatsUtils;
import org.openimaj.util.array.ArrayUtils;

/**
 * Subtract the mean of each example vector from itself to normalise the vector.
 * <p>
 * Only use if the data is stationary (i.e., the statistics for each data
 * dimension follow the same distribution).
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class PerExampleMeanCenter implements Normaliser {
	@Override
	public double[] normalise(double[] vector) {
		final double mean = DoubleArrayStatsUtils.mean(vector);
		vector = ArrayUtils.subtract(vector, mean);
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
