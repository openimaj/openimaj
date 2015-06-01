package org.openimaj.math.statistics.normalisation;

/**
 * Interface for objects that con both normalise and denormalise data.
 * Denormalising should return the data to it's original state before
 * normalisation, although in certain circumstances, the process might be lossy.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public interface Denormaliser extends Normaliser {
	/**
	 * Deormalise the vector.
	 *
	 * @param vector
	 *            the normalised vector
	 * @return the denormalised vector
	 */
	public double[] denormalise(double[] vector);

	/**
	 * Denormalise the data.
	 *
	 * @param data
	 *            the normalised data (one normalised vector per row)
	 * @return the denormalised data
	 */
	public double[][] denormalise(double[][] data);
}
