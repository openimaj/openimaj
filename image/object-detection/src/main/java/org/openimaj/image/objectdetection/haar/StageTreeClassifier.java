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
 * A tree of classifier stages. In the case that the tree is degenerate and all
 * {@link Stage}s have <code>null</code> {@link Stage#failureStage()}s, then the
 * tree is known as a <strong>cascade</strong>.
 * <p>
 * The general idea is that for a given window in the image being tested
 * (defined by an x,y position and scale), the stage tree is evaluated. If when
 * evaluating the tree a leaf node is hit (i.e. a {@link Stage} that passes
 * successfully, but has a <code>null</code> {@link Stage#successStage()}) then
 * the tree is said to have passed, and indicates a potential object detection
 * within the window. If a {@link Stage} fails to pass and has a
 * <code>null</code> {@link Stage#failureStage()} then the tree is said to have
 * failed, indicating the object in question was not found.
 * <p>
 * In order to achieve good performance, this implementation pre-computes and
 * caches variables related to a given detection scale. This means that it is
 * <strong>NOT safe</strong> to use a detector based on this stage
 * implementation in a multi-threaded environment such that multiple images are
 * being tested at a given time. It is however safe to use this implementation
 * with a detector that multi-threads its detection across the x and y window
 * positions for a fixed scale:
 *
 * <code><pre>
 *  StageTreeClassifier cascade = ...
 * 
 * 	for each scale {
 * 		cascade.setScale(scale);
 * 
 * 		//the x and y search could be threaded...
 * 		for each y {
 * 			for each x {
 * 				cascade.matches(sat, x, y); {
 * 			}
 * 		}
 * }
 * </pre></code>
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
				"keywords",
				" AdaBoost; background regions; boosted simple feature cascade; classifiers; face detection; image processing; image representation; integral image; machine learning; object specific focus-of-attention mechanism; rapid object detection; real-time applications; statistical guarantees; visual object detection; feature extraction; image classification; image representation; learning (artificial intelligence); object detection;",
				"doi", "10.1109/CVPR.2001.990517",
				"ISSN", "1063-6919 "
		})
public class StageTreeClassifier {
	/**
	 * The width of the classifier
	 */
	int width;

	/**
	 * The height of the classifier
	 */
	int height;

	/**
	 * The name of the classifier
	 */
	String name;

	/**
	 * Does the classifier contain tilted features?
	 */
	boolean hasTiltedFeatures;

	/**
	 * The root of the stage tree
	 */
	Stage root;

	// cached values for the scale being processed
	float cachedScale; // the current scale
	float cachedInvArea; // the inverse of the current (scaled) detection window
	int cachedW; // the width of the current (scaled) detection window
	int cachedH; // the height of the current (scaled) detection window

	/**
	 * Construct the {@link StageTreeClassifier} with the given parameters.
	 *
	 * @param width
	 *            the width of the classifier
	 * @param height
	 *            the height of the classifier
	 * @param name
	 *            the name of the classifier
	 * @param hasTiltedFeatures
	 *            are there tilted haar-like features in the classifiers?
	 * @param root
	 *            the root of the tree of stages
	 */
	public StageTreeClassifier(int width, int height, String name, boolean hasTiltedFeatures, Stage root) {
		this.width = width;
		this.height = height;
		this.name = name;
		this.hasTiltedFeatures = hasTiltedFeatures;
		this.root = root;
	}

	float computeWindowVarianceNorm(SummedSqTiltAreaTable sat, int x, int y) {
		x += Math.round(cachedScale); // shift by 1 scaled px to centre box
		y += Math.round(cachedScale);

		final float sum = sat.sum.pixels[y + cachedH][x + cachedW] + sat.sum.pixels[y][x] -
				sat.sum.pixels[y + cachedH][x] - sat.sum.pixels[y][x + cachedW];
		final float sqSum = sat.sqSum.pixels[y + cachedH][x + cachedW] + sat.sqSum.pixels[y][x] -
				sat.sqSum.pixels[y + cachedH][x] - sat.sqSum.pixels[y][x + cachedW];

		final float mean = sum * cachedInvArea;
		float wvNorm = sqSum * cachedInvArea - mean * mean;
		wvNorm = (float) ((wvNorm > 0) ? Math.sqrt(wvNorm) : 1);

		return wvNorm;
	}

	/**
	 * Set the current detection scale. This must be called before calling
	 * {@link #classify(SummedSqTiltAreaTable, int, int)}.
	 * <p>
	 * Internally, this goes through all the stages and their individual
	 * classifiers and pre-caches information related to the current scale to
	 * avoid lots of expensive recomputation of values that don't change for a
	 * given scale.
	 *
	 * @param scale
	 *            the current scale
	 */
	public void setScale(float scale) {
		this.cachedScale = scale;

		// following the OCV code... -2 to make a slightly smaller box within
		// window
		cachedW = Math.round(scale * (width - 2));
		cachedH = Math.round(scale * (height - 2));
		cachedInvArea = 1.0f / (cachedW * cachedH);

		updateCaches(root);
	}

	/**
	 * Recursively update the caches of all the stages to reflect the current
	 * scale.
	 *
	 * @param s
	 *            the stage to update
	 */
	private void updateCaches(Stage s) {
		s.updateCaches(this);

		if (s.successStage != null)
			updateCaches(s.successStage);
		if (s.failureStage != null)
			updateCaches(s.failureStage);
	}

	/**
	 * Apply the classifier to the given image at the given position.
	 * Internally, this will apply each stage to the image. If all stages
	 * complete successfully a detection is indicated.
	 * <p>
	 * This method returns the number of stages passed if all stages pass; if a
	 * stage fails, then (-1 * number of successful stages) is returned. For
	 * example a value of 20 indicates the successful detection from a total of
	 * 20 stages, whilst -10 indicates an unsuccessful detection due to a
	 * failure on the 11th stage.
	 *
	 * @param sat
	 *            the summed area table(s) for the image in question. If there
	 *            are tilted features, this must include the tilted SAT.
	 * @param x
	 *            the x-ordinate of the top-left of the current window
	 * @param y
	 *            the y-ordinate of the top-left of the current window
	 * @return > 0 if a detection was made; <=0 if no detection was made. The
	 *         magnitude indicates the number of stages that passed.
	 */
	public int classify(SummedSqTiltAreaTable sat, int x, int y) {
		final float wvNorm = computeWindowVarianceNorm(sat, x, y);

		// all stages need to match for this cascade to match
		int matches = 0; // the number of stages that pass
		Stage stage = root;
		while (true) { // until success or failure
			if (stage.pass(sat, wvNorm, x, y)) {
				matches++;
				stage = stage.successStage;
				if (stage == null) {
					return matches;
				}
			} else {
				stage = stage.failureStage;
				if (stage == null) {
					return -matches;
				}
			}
		}
	}

	/**
	 * Get the classifier width
	 *
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Get the classifier height
	 *
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Get the classifier name
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Does the classifier use tilted haar-like features?
	 *
	 * @return true if tilted features are used; false otherwise.
	 */
	public boolean hasTiltedFeatures() {
		return hasTiltedFeatures;
	}

	/**
	 * Get the root {@link Stage} of the classifier
	 *
	 * @return the root
	 */
	public Stage getRoot() {
		return root;
	}
}
