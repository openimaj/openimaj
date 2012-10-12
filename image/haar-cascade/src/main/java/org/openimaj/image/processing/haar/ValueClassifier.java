package org.openimaj.image.processing.haar;

import org.openimaj.image.analysis.algorithm.SummedSqTiltAreaTable;

/**
 * A classifier that just returns a constant value.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public final class ValueClassifier implements Classifier {
	float value;

	/**
	 * Construct with the given value.
	 * 
	 * @param value
	 *            the value.
	 */
	public ValueClassifier(float value) {
		this.value = value;
	}

	@Override
	public final float classify(final SummedSqTiltAreaTable sat, final float wvNorm, final int x, final int y) {
		return value;
	}

	@Override
	public void updateCaches(StageTreeClassifier cascade) {
		// do nothing
	}
}
