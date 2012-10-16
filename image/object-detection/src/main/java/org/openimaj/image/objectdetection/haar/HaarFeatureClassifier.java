package org.openimaj.image.objectdetection.haar;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
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
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Viola, P.", "Jones, M." },
		title = "Rapid object detection using a boosted cascade of simple features",
		year = "2001",
		booktitle = "Computer Vision and Pattern Recognition, 2001. CVPR 2001. Proceedings of the 2001 IEEE Computer Society Conference on",
		pages = { " I", "511 ", " I", "518 vol.1" },
		number = "",
		volume = "1",
		customData = {
				"keywords", " AdaBoost; background regions; boosted simple feature cascade; classifiers; face detection; image processing; image representation; integral image; machine learning; object specific focus-of-attention mechanism; rapid object detection; real-time applications; statistical guarantees; visual object detection; feature extraction; image classification; image representation; learning (artificial intelligence); object detection;",
				"doi", "10.1109/CVPR.2001.990517",
				"ISSN", "1063-6919 "
		})
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
