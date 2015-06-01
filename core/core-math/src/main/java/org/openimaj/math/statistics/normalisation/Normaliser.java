package org.openimaj.math.statistics.normalisation;

/**
 * Interface describing an object that can apply normalisation to some data
 * vectors.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public interface Normaliser {
	/**
	 * Normalise the vector.
	 *
	 * @param vector
	 *            the vector
	 * @return the normalised vector
	 */
	public abstract double[] normalise(double[] vector);

	/**
	 * Normalise the data.
	 *
	 * @param data
	 *            the data (one vector per row)
	 * @return the normalised data
	 */
	public double[][] normalise(double[][] data);
}
