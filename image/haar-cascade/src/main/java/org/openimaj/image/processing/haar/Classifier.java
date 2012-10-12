package org.openimaj.image.processing.haar;

import org.openimaj.image.analysis.algorithm.SummedSqTiltAreaTable;

/**
 * Interface for an individual Haar-like classifier.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public interface Classifier {
	/**
	 * Get the classification score for the window at (x, y) with the size
	 * defined by scale.
	 * 
	 * @param sat
	 *            the summed area tables (integral images)
	 * @param wvNorm
	 *            the normalisation based on the current window variance
	 * @param x
	 *            the x-ordinate of the top-left of the window being tested
	 * @param y
	 *            the y-ordinate of the top-left of the window being tested
	 * @return the classification score
	 */
	public float classify(final SummedSqTiltAreaTable sat, final float wvNorm, final int x, final int y);

	/**
	 * Update the caches for a given scale (given by the
	 * <code>cachedScale</code> of the {@link StageTreeClassifier}).
	 * 
	 * @param cascade
	 *            the tree of stages
	 */
	void updateCaches(StageTreeClassifier cascade);
}
