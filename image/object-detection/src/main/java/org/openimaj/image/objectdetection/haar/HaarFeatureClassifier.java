/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
