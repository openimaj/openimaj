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
package org.openimaj.image.feature.dense.gradient;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.analysis.algorithm.histogram.BinnedWindowedExtractor;
import org.openimaj.image.analysis.algorithm.histogram.GradientOrientationHistogramExtractor;
import org.openimaj.image.analysis.algorithm.histogram.InterpolatedBinnedWindowedExtractor;
import org.openimaj.image.analysis.algorithm.histogram.binning.QuadtreeStrategy;
import org.openimaj.image.pixel.sampling.QuadtreeSampler;
import org.openimaj.image.processing.convolution.FImageGradients;
import org.openimaj.image.processing.convolution.FImageGradients.Mode;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * This class is an implementation of an extractor for the PHOG (Pyramid
 * Histograms of Orientation Gradients) feature described by Bosch et al. The
 * PHOG feature is computed by creating a quadtree of orientation histograms
 * over the entire image and appending the histograms for each cell of the
 * quadtree into a single vector which is then l1 normalised (sum to unity).
 * <p>
 * In the original description, only orientations at edge pixels were counted;
 * that restriction is optional in this implementation. If only edge pixels are
 * used, then the feature describes the distribution of <b>shape</b> in the
 * image. Conversely, if all pixels are used, the feature essentially describes
 * the texture of the image.
 * <p>
 * As this class will typically be used to only construct a single feature from
 * an image, it is built around a {@link BinnedWindowedExtractor} (or
 * {@link InterpolatedBinnedWindowedExtractor} if interpolation is used). This
 * will be much more efficient than a
 * {@link GradientOrientationHistogramExtractor} in the single window case. If
 * you do need to extract many PHOG-like features different rectangles of the
 * same image, use a {@link GradientOrientationHistogramExtractor} coupled with
 * a {@link QuadtreeStrategy} to achieve the desired effect.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Bosch, Anna", "Zisserman, Andrew", "Munoz, Xavier" },
		title = "Representing shape with a spatial pyramid kernel",
		year = "2007",
		booktitle = "Proceedings of the 6th ACM international conference on Image and video retrieval",
		pages = { "401", "", "408" },
		url = "http://doi.acm.org/10.1145/1282280.1282340",
		publisher = "ACM",
		series = "CIVR '07",
		customData = {
				"isbn", "978-1-59593-733-9",
				"location", "Amsterdam, The Netherlands",
				"numpages", "8",
				"doi", "10.1145/1282280.1282340",
				"acmid", "1282340",
				"address", "New York, NY, USA",
				"keywords", "object and video retrieval, shape features, spatial pyramid kernel"
		})
public class PHOG implements ImageAnalyser<FImage>, FeatureVectorProvider<DoubleFV> {
	private int nlevels = 3;
	private ImageProcessor<FImage> edgeDetector;
	private Mode orientationMode;

	private BinnedWindowedExtractor histExtractor;
	private Rectangle lastBounds;
	private FImage magnitudes;

	/**
	 * Construct with the values used in the paper: 4 levels (corresponds to l=3
	 * in the paper), 40 orientation bins (interpolated), signed gradients
	 * (called "shape360" in the original paper) and Canny edge detection.
	 */
	public PHOG() {
		this(4, 40, FImageGradients.Mode.Signed);
	}

	/**
	 * Construct with the given values, using Canny edge detection and gradient
	 * histogram interpolation.
	 *
	 * @param nlevels
	 *            number of pyramid levels (note this includes l0, so you might
	 *            need 1 more)
	 * @param nbins
	 *            number of bins
	 * @param orientationMode
	 *            the orientation mode
	 */
	public PHOG(int nlevels, int nbins, FImageGradients.Mode orientationMode)
	{
		this(nlevels, nbins, true, orientationMode, new CannyEdgeDetector());
	}

	/**
	 * Construct with the given parameters. The <code>edgeDetector</code>
	 * parameter can be <code>null</code> if you don't want to filter out
	 * non-edge pixels from the histograms.
	 *
	 * @param nlevels
	 *            number of pyramid levels (note this includes l0, so you might
	 *            need 1 more)
	 * @param nbins
	 *            number of bins
	 * @param histogramInterpolation
	 *            should the gradient orientations be interpolated?
	 * @param orientationMode
	 *            the orientation mode
	 * @param edgeDetector
	 *            the edge detector to use (may be <code>null</code> for
	 *            gradient features)
	 */
	public PHOG(int nlevels, int nbins, boolean histogramInterpolation, FImageGradients.Mode orientationMode,
			ImageProcessor<FImage> edgeDetector)
	{
		this.nlevels = nlevels;
		this.edgeDetector = edgeDetector;
		this.orientationMode = orientationMode;

		if (histogramInterpolation)
			histExtractor = new InterpolatedBinnedWindowedExtractor(nbins, true);
		else
			histExtractor = new BinnedWindowedExtractor(nbins);

		histExtractor.setMax(orientationMode.maxAngle());
		histExtractor.setMin(orientationMode.minAngle());
	}

	@Override
	public void analyseImage(FImage image) {
		lastBounds = image.getBounds();

		final FImageGradients gradMag = FImageGradients.getGradientMagnitudesAndOrientations(image, orientationMode);
		this.magnitudes = gradMag.magnitudes;

		histExtractor.analyseImage(gradMag.orientations);

		if (edgeDetector != null) {
			magnitudes.multiplyInplace(image.process(edgeDetector));
		}
	}

	/**
	 * Extract the PHOG feature for the specified region of the image last
	 * analysed with {@link #analyseImage(FImage)}.
	 *
	 * @param rect
	 *            the region
	 * @return the PHOG feature
	 */
	public Histogram getFeatureVector(Rectangle rect) {
		final QuadtreeSampler sampler = new QuadtreeSampler(rect, nlevels + 1);
		Histogram hist = new Histogram(0);

		for (final Rectangle r : sampler) {
			final Histogram h = histExtractor.computeHistogram(r, magnitudes);
			hist = hist.combine(h);
		}

		hist.normaliseL1();

		return hist;
	}

	/**
	 * Extract the PHOG feature for the whole of the image last analysed with
	 * {@link #analyseImage(FImage)}.
	 *
	 * @return the PHOG feature
	 *
	 * @see org.openimaj.feature.FeatureVectorProvider#getFeatureVector()
	 */
	@Override
	public Histogram getFeatureVector() {
		return getFeatureVector(lastBounds);
	}
}
