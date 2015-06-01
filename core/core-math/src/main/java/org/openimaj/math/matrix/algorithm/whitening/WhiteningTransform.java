package org.openimaj.math.matrix.algorithm.whitening;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;

/**
 * Abstract base class for whitening transforms ("sphering"). Whitening
 * transforms rescale data to remove correlations; more specifically the idea of
 * whitening is that it completely removes the second-order information
 * (correlations and (co)-variances) of the data.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
@Reference(
		type = ReferenceType.Book,
		author = { "Hyvrinen, Aapo", "Hurri, Jarmo", "Hoyer, Patrick O." },
		title = "Natural Image Statistics: A Probabilistic Approach to Early Computational Vision.",
		year = "2009",
		edition = "1st",
		publisher = "Springer Publishing Company, Incorporated",
		customData = {
				"isbn", "1848824904, 9781848824904"
		})
public abstract class WhiteningTransform {
	/**
	 * Apply the whitening transform to the given vector.
	 *
	 * @param vector
	 *            the vector
	 * @return the whitened vector
	 */
	public abstract double[] whiten(double[] vector);

	/**
	 * Apply the whitening transform to the given vectors.
	 *
	 * @param vector
	 *            the vectors; one per row
	 * @return the whitened vector
	 */
	public double[][] whiten(double[][] vector) {
		final double[][] out = new double[vector.length][];
		for (int i = 0; i < vector.length; i++) {
			out[i] = whiten(vector[i]);
		}
		return out;
	}

	/**
	 * Train the whitening transform with the given features.
	 *
	 * @param data
	 *            the data (one feature per row)
	 */
	public abstract void train(double[][] data);
}
