package org.openimaj.feature.normalisation;

import org.openimaj.feature.FloatFV;

/**
 * This {@link Normaliser} normalises vectors such that the Euclidean distance
 * between normalised vectors is equivalent to computing the similarity using
 * the Hellinger kernel on the un-normalised vectors.
 * <p>
 * The normalisation works by optionally adding an offset to the vectors (to
 * deal with input vectors that have negative values), L1, normalising the
 * vectors and finally performing an element-wise sqrt.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class HellingerNormaliser implements Normaliser<FloatFV> {
	int offset;

	public HellingerNormaliser() {
		this.offset = 0;
	}

	public HellingerNormaliser(int offset) {
		this.offset = offset;
	}

	@Override
	public void normalise(FloatFV feature) {
		double sum = 0;

		for (int i = 0; i < feature.values.length; i++) {
			feature.values[i] += offset;
			sum += feature.values[i];
		}

		for (int i = 0; i < feature.values.length; i++) {
			feature.values[i] = (float) Math.sqrt(feature.values[i] / sum);
		}
	}
}
