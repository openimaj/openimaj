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

import java.util.ArrayList;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.SummedSqTiltAreaTable;
import org.openimaj.image.objectdetection.AbstractMultiScaleObjectDetector;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Basic, single-threaded multi-scale Haar cascade/tree object detector. The
 * detector determines a range of scales to search based on an optional minimum
 * and maximum detection size (if not specified, minimum is the size of the
 * {@link StageTreeClassifier} and maximum is the image size), and a
 * scale-factor which determines the amount to change between scales. At a given
 * scale, the entire image is searched. In order to speed-up detection, if no
 * detection is made for a given (x, y) coordinate, the x-ordinate position is
 * incremented by {@link #bigStep()}, otherwise it is incremented by
 * {@link #smallStep()}.
 * <p>
 * <strong>Important note:</strong> This detector is NOT thread-safe due to the
 * fact that {@link StageTreeClassifier}s are not themselves thread-safe. Do not
 * attempt to use it in a multi-threaded environment!
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
public class Detector extends AbstractMultiScaleObjectDetector<FImage, Rectangle> {
	/**
	 * Default step size to make when there is a hint of detection.
	 */
	public static final int DEFAULT_SMALL_STEP = 1;

	/**
	 * Default step size to make when there is definitely no detection.
	 */
	public static final int DEFAULT_BIG_STEP = 2;

	/**
	 * Default scale factor multiplier.
	 */
	public static final float DEFAULT_SCALE_FACTOR = 1.1f;

	protected StageTreeClassifier cascade;
	protected float scaleFactor = 1.1f;
	protected int smallStep = 1;
	protected int bigStep = 2;

	/**
	 * Construct the {@link Detector} with the given parameters.
	 * 
	 * @param cascade
	 *            the cascade or tree of stages.
	 * @param scaleFactor
	 *            the amount to change between scales (multiplicative)
	 * @param smallStep
	 *            the amount to step when there is a hint of detection
	 * @param bigStep
	 *            the amount to step when there is definitely no detection
	 */
	public Detector(StageTreeClassifier cascade, float scaleFactor, int smallStep, int bigStep) {
		super(Math.max(cascade.width, cascade.height), 0);

		this.cascade = cascade;
		this.scaleFactor = scaleFactor;
		this.smallStep = smallStep;
		this.bigStep = bigStep;
	}

	/**
	 * Construct the {@link Detector} with the given tree of stages and scale
	 * factor. The default step sizes are used.
	 * 
	 * @param cascade
	 *            the cascade or tree of stages.
	 * @param scaleFactor
	 *            the amount to change between scales
	 */
	public Detector(StageTreeClassifier cascade, float scaleFactor) {
		this(cascade, scaleFactor, DEFAULT_SMALL_STEP, DEFAULT_BIG_STEP);
	}

	/**
	 * Construct the {@link Detector} with the given tree of stages, and the
	 * default parameters for step sizes and scale factor.
	 * 
	 * @param cascade
	 *            the cascade or tree of stages.
	 */
	public Detector(StageTreeClassifier cascade) {
		this(cascade, DEFAULT_SCALE_FACTOR, DEFAULT_SMALL_STEP, DEFAULT_BIG_STEP);
	}

	/**
	 * Perform detection at a single scale. Subclasses may override this to
	 * customise the spatial search. The given starting and stopping coordinates
	 * take into account any region of interest set on this detector.
	 * 
	 * @param sat
	 *            the summed area table(s)
	 * @param startX
	 *            the starting x-ordinate
	 * @param stopX
	 *            the stopping x-ordinate
	 * @param startY
	 *            the starting y-ordinate
	 * @param stopY
	 *            the stopping y-ordinate
	 * @param ystep
	 *            the amount to step
	 * @param windowWidth
	 *            the window width at the current scale
	 * @param windowHeight
	 *            the window height at the current scale
	 * @param results
	 *            the list to store detection results in
	 */
	protected void detectAtScale(final SummedSqTiltAreaTable sat, final int startX, final int stopX, final int startY,
			final int stopY, final float ystep, final int windowWidth, final int windowHeight,
			final List<Rectangle> results)
	{
		for (int iy = startY; iy < stopY; iy++) {
			final int y = Math.round(iy * ystep);

			for (int ix = startX, xstep = 0; ix < stopX; ix += xstep) {
				final int x = Math.round(ix * ystep);

				final int result = cascade.classify(sat, x, y);

				if (result > 0) {
					results.add(new Rectangle(x, y, windowWidth, windowHeight));
				}

				// if there is no detection, then increase the step size
				xstep = (result > 0 ? smallStep : bigStep);

				// TODO: think about what to do if there isn't a detection, but
				// we're very close to having one based on the ratio of stages
				// passes to total stages.
			}
		}
	}

	@Override
	public List<Rectangle> detect(FImage image) {
		final List<Rectangle> results = new ArrayList<Rectangle>();

		final int imageWidth = image.getWidth();
		final int imageHeight = image.getHeight();

		final SummedSqTiltAreaTable sat = new SummedSqTiltAreaTable(image, cascade.hasTiltedFeatures);

		// compute the number of scales to test and the starting factor
		int nFactors = 0;
		int startFactor = 0;
		for (float factor = 1; factor * cascade.width < imageWidth - 10 &&
				factor * cascade.height < imageHeight - 10; factor *= scaleFactor)
		{
			final float width = factor * cascade.width;
			final float height = factor * cascade.height;

			if (width < minSize || height < minSize) {
				startFactor++;
			}

			if (maxSize > 0 && (width > maxSize || height > maxSize)) {
				break;
			}

			nFactors++;
		}

		// run the detection at each scale
		float factor = (float) Math.pow(scaleFactor, startFactor);
		for (int scaleStep = startFactor; scaleStep < nFactors; factor *= scaleFactor, scaleStep++) {
			final float ystep = Math.max(2, factor);

			final int windowWidth = (int) (factor * cascade.width);
			final int windowHeight = (int) (factor * cascade.height);

			// determine the spatial range, taking into account any ROI.
			final int startX = (int) (roi == null ? 0 : Math.max(0, roi.x));
			final int startY = (int) (roi == null ? 0 : Math.max(0, roi.y));
			final int stopX = Math.round(
					(((roi == null ? imageWidth : Math.min(imageWidth, roi.x + roi.width)) - windowWidth)) / ystep);
			final int stopY = Math.round(
					(((roi == null ? imageHeight : Math.min(imageHeight, roi.y + roi.height)) - windowHeight)) / ystep);

			// prepare the cascade for this scale
			cascade.setScale(factor);

			detectAtScale(sat, startX, stopX, startY, stopY, ystep, windowWidth, windowHeight, results);
		}

		return results;
	}

	/**
	 * Get the step size the detector will make if there is any hint of a
	 * detection. This should be smaller than {@link #bigStep()}.
	 * 
	 * @return the amount to step on any hint of detection.
	 */
	public int smallStep() {
		return smallStep;
	}

	/**
	 * Get the step size the detector will make if there is definitely no
	 * detection. This should be bigger than {@link #smallStep()}.
	 * 
	 * @return the amount to step when there is definitely no detection.
	 */
	public int bigStep() {
		return bigStep;
	}

	/**
	 * Set the step size the detector will make if there is any hint of a
	 * detection. This should be smaller than {@link #bigStep()}.
	 * 
	 * @param smallStep
	 *            The amount to step on any hint of detection.
	 */
	public void setSmallStep(int smallStep) {
		this.smallStep = smallStep;
	}

	/**
	 * Set the step size the detector will make if there is definitely no
	 * detection. This should be bigger than {@link #smallStep()}.
	 * 
	 * @param bigStep
	 *            The amount to step when there is definitely no detection.
	 */
	public void bigStep(int bigStep) {
		this.bigStep = bigStep;
	}

	/**
	 * Get the scale factor (the amount to change between scales
	 * (multiplicative)).
	 * 
	 * @return the scaleFactor
	 */
	public float getScaleFactor() {
		return scaleFactor;
	}

	/**
	 * Set the scale factor (the amount to change between scales
	 * (multiplicative)).
	 * 
	 * @param scaleFactor
	 *            the scale factor to set
	 */
	public void setScaleFactor(float scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	/**
	 * Get the classifier tree or cascade used by this detector.
	 * 
	 * @return the classifier tree or cascade.
	 */
	public StageTreeClassifier getClassifier() {
		return cascade;
	}
}
