package org.openimaj.feature.normalisation;

import org.openimaj.feature.FloatFV;

/**
 * This {@link Normaliser} normalises vectors such that the Euclidean distance
 * between normalised vectors is equivalent to computing the similarity using
 * the Hellinger kernel on the un-normalised vectors.
 * <p>
 * The normalisation works by optionally adding an offset to the vectors (to
 * deal with input vectors that have negative values), L1 normalising the
 * vectors and finally performing an element-wise sqrt.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class HellingerNormaliser implements Normaliser<FloatFV> {
	protected int offset;

	/**
	 * Construct with no offset
	 */
	public HellingerNormaliser() {
		this.offset = 0;
	}

	/**
	 * Construct with the given offset
	 * 
	 * @param offset
	 *            the offset
	 */
	public HellingerNormaliser(int offset) {
		this.offset = offset;
	}

	@Override
	public void normalise(FloatFV feature) {
		normalise(feature.values, offset);
	}

	/**
	 * Static utility function to perform Hellinger normalisation.
	 * 
	 * @param values
	 *            the values to normalise
	 * @param offset
	 *            the offset to add to the values before normalisation (to
	 *            ensure they are +ve).
	 */
	public static void normalise(float[] values, int offset) {
		double sum = 0;

		for (int i = 0; i < values.length; i++) {
			values[i] += offset;
			sum += values[i];
		}

		for (int i = 0; i < values.length; i++) {
			values[i] = (float) Math.sqrt(values[i] / sum);
		}
	}
}
