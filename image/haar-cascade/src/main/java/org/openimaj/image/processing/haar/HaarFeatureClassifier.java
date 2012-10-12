package org.openimaj.image.processing.haar;

import org.openimaj.image.analysis.algorithm.SummedSqTiltAreaTable;

/**
 * A classifier based on a Haar-like feature. The classifier forms a binary tree
 * (or stump) and has left and right nodes to apply depending on the outcome of
 * feature evaluation. If this classifier is a stump, then its left and right
 * nodes will be {@link ValueClassifier}s. If it is a tree, then the left and/or
 * right nodes will be {@link HaarFeatureClassifier}s.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class HaarFeatureClassifier implements Classifier {
	Classifier left;
	Classifier right;
	HaarFeature feature;
	float threshold;

	/**
	 * Construct with the given feature, threshold and left/right nodes.
	 * 
	 * @param feature
	 *            the feature on which the classifier is based.
	 * @param threshold
	 *            the threshold for the classifier.
	 * @param left
	 *            the classifier to invoke if the feature response is less than
	 *            the threshold
	 * @param right
	 *            the classifier to invoke if the feature response is greater
	 *            than or equal to the threshold
	 */
	public HaarFeatureClassifier(HaarFeature feature, float threshold, Classifier left, Classifier right) {
		this.feature = feature;
		this.threshold = threshold;
		this.left = left;
		this.right = right;
	}

	@Override
	public float classify(final SummedSqTiltAreaTable sat, final float wvNorm,
			final int x, final int y)
	{
		final float response = feature.computeResponse(sat, x, y);

		return (response < threshold * wvNorm) ?
				left.classify(sat, wvNorm, x, y) :
					right.classify(sat, wvNorm, x, y);
	}

	@Override
	public void updateCaches(StageTreeClassifier cascade) {
		feature.updateCaches(cascade);
		left.updateCaches(cascade);
		right.updateCaches(cascade);
	}
}
