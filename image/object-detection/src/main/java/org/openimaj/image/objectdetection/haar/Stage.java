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
 * A classification stage. The stage is made up of an ensemble of classifiers
 * (which may themselves be trees or stumps). In order to evaluate the stage,
 * the ensemble of classifiers is tested in order and the responses are summed.
 * If the summed response exceeds a threshold (corresponding to a certain
 * error-rate set during training) then the stage will pass and the
 * {@link #successStage()} should be evaluated. If the stage fails, then the
 * {@link #failureStage()} will be evaluated. The actual coordination of calling
 * the {@link #successStage()} or {@link #failureStage()} is performed by the
 * {@link StageTreeClassifier}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
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
public class Stage {
	float threshold;
	Classifier[] ensemble;

	/**
	 * The next stage to evaluate should this one pass; if this stage passes and
	 * this is <code>null</code> then the whole {@link StageTreeClassifier}
	 * passes.
	 */
	Stage successStage;

	/**
	 * The next stage to evaluate should this one fail; if this stage fails and
	 * this is <code>null</code> then the whole {@link StageTreeClassifier}
	 * fails.
	 */
	Stage failureStage;

	/**
	 * Do any of the {@link ValueClassifier}s have negative values? If not, we
	 * can perform an optimisation in
	 * {@link #pass(StageTreeClassifier, SummedSqTiltAreaTable, float, int, int)}
	 * to shortcut checking the entire ensemble.
	 */
	private boolean hasNegativeValueFeatures;

	/**
	 * Construct a new stage.
	 * 
	 * @param threshold
	 *            the threshold for the stage to pass
	 * @param trees
	 *            the classifier trees in the stage
	 * @param successStage
	 *            the next stage after this one, or null if this is the last
	 * @param failureStage
	 *            the failure stage (for trees rather than cascades)
	 */
	public Stage(float threshold, Classifier[] trees, Stage successStage, Stage failureStage) {
		this.threshold = threshold;
		this.ensemble = trees;
		this.successStage = successStage;
		this.failureStage = failureStage;

		this.hasNegativeValueFeatures = checkForNegativeValueFeatures();
	}

	private boolean checkForNegativeValueFeatures() {
		for (int i = 0; i < ensemble.length; i++) {
			if (checkForNegativeValueFeatures(ensemble[i]))
				return true;
		}
		return false;
	}

	private boolean checkForNegativeValueFeatures(Classifier classifier) {
		if (classifier instanceof ValueClassifier) {
			if (((ValueClassifier) classifier).value < 0)
				return true;
		} else {
			if (checkForNegativeValueFeatures(((HaarFeatureClassifier) classifier).left))
				return true;
			if (checkForNegativeValueFeatures(((HaarFeatureClassifier) classifier).right))
				return true;
		}

		return false;
	}

	/**
	 * Test whether a stage passes. For the stage to pass, the sum of the
	 * responses from the internal classification trees or stumps must exceed
	 * the stage's threshold.
	 * 
	 * @param sat
	 *            the summed area tables (integral images)
	 * @param wvNorm
	 *            the normalisation based on the current window variance
	 * @param x
	 *            the x-ordinate of the top-left of the window being tested
	 * @param y
	 *            the y-ordinate of the top-left of the window being tested
	 * @return true if the stage passes; false otherwise
	 */
	public boolean pass(final SummedSqTiltAreaTable sat, final float wvNorm,
			final int x, final int y)
	{
		float total = 0;

		// Optimisation: if there are no negative valued features in the
		// ensemble, then the sum can only increase & it is cheaper to perform
		// the threshold check on each iteration
		if (hasNegativeValueFeatures) {
			for (int i = 0; i < ensemble.length; i++) {
				total += ensemble[i].classify(sat, wvNorm, x, y);
			}

			return total >= threshold;
		} else {
			for (int i = 0; i < ensemble.length; i++) {
				total += ensemble[i].classify(sat, wvNorm, x, y);
				if (total >= threshold)
					return true;
			}

			return false;
		}
	}

	/**
	 * Update the caches for a given scale (given by
	 * {@link StageTreeClassifier#cachedScale}). The {@link Stage} itself
	 * doesn't cache anything, but it's classifiers might...
	 * 
	 * @param cascade
	 *            the tree of stages
	 */
	void updateCaches(StageTreeClassifier cascade) {
		for (int i = 0; i < ensemble.length; i++) {
			ensemble[i].updateCaches(cascade);
		}
	}

	/**
	 * Get the next stage to evaluate should this one pass; if this stage passes
	 * and this is <code>null</code> then the whole {@link StageTreeClassifier}
	 * passes.
	 * 
	 * @return the next stage should
	 *         {@link #pass(SummedSqTiltAreaTable, float, int, int)} return
	 *         true.
	 */
	public Stage successStage() {
		return successStage;
	}

	/**
	 * Get the next stage to evaluate should this one fail; if this stage fails
	 * and this is <code>null</code> then the whole {@link StageTreeClassifier}
	 * fails.
	 * 
	 * @return the next stage should
	 *         {@link #pass(SummedSqTiltAreaTable, float, int, int)} return
	 *         false.
	 */
	public Stage failureStage() {
		return failureStage;
	}
}
