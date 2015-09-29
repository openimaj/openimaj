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
import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.analysis.algorithm.histogram.GradientOrientationHistogramExtractor;
import org.openimaj.image.analysis.algorithm.histogram.binning.SpatialBinningStrategy;
import org.openimaj.image.feature.dense.gradient.binning.FixedHOGStrategy;
import org.openimaj.image.feature.dense.gradient.binning.FlexibleHOGStrategy;
import org.openimaj.image.processing.convolution.FImageGradients;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * Implementation of an extractor for the Histogram of Oriented Gradients (HOG)
 * feature for object detection. This implementation allows any kind of spatial
 * layout to be used through different implementations of
 * {@link SpatialBinningStrategy}s. HOG features can be efficiently extracted
 * for many windows of the image.
 * <p>
 * The actual work of computing and normalising the descriptor is performed by
 * the {@link SpatialBinningStrategy} (i.e. a {@link FixedHOGStrategy} or
 * {@link FlexibleHOGStrategy}); this class just provides the objects required
 * for efficient histogram computation (namely a
 * {@link GradientOrientationHistogramExtractor}) for the image being analysed.
 * <p>
 * Normally, HOG features are computed using all gradients in the image, but
 * this class makes it possible to only consider gradients along "edges" using
 * the {@link #analyseImage(FImage, FImage)} method.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Dalal, Navneet", "Triggs, Bill" },
		title = "Histograms of Oriented Gradients for Human Detection",
		year = "2005",
		booktitle = "Proceedings of the 2005 IEEE Computer Society Conference on Computer Vision and Pattern Recognition (CVPR'05) - Volume 1 - Volume 01",
		pages = { "886", "", "893" },
		url = "http://dx.doi.org/10.1109/CVPR.2005.177",
		publisher = "IEEE Computer Society",
		series = "CVPR '05",
		customData = {
				"isbn", "0-7695-2372-2",
				"numpages", "8",
				"doi", "10.1109/CVPR.2005.177",
				"acmid", "1069007",
				"address", "Washington, DC, USA"
		})
public class HOG implements ImageAnalyser<FImage> {
	GradientOrientationHistogramExtractor extractor;
	protected SpatialBinningStrategy strategy;

	private transient Histogram currentHist;

	/**
	 * Construct a new {@link HOG} with the 9 bins, using histogram
	 * interpolation and unsigned gradients. Use the given strategy to extract
	 * the actual features.
	 *
	 * @param strategy
	 *            the {@link SpatialBinningStrategy} to use to produce the
	 *            features
	 */
	public HOG(SpatialBinningStrategy strategy)
	{
		this(9, true, FImageGradients.Mode.Unsigned, strategy);
	}

	/**
	 * Construct a new {@link HOG} with the given number of bins. Optionally
	 * perform linear interpolation across orientation bins. Histograms can also
	 * use either signed or unsigned gradients.
	 *
	 * @param nbins
	 *            number of bins
	 * @param histogramInterpolation
	 *            if true cyclic linear interpolation is used to share the
	 *            magnitude across the two closest bins; if false only the
	 *            closest bin will be filled.
	 * @param orientationMode
	 *            the range of orientations to extract
	 * @param strategy
	 *            the {@link SpatialBinningStrategy} to use to produce the
	 *            features
	 */
	public HOG(int nbins, boolean histogramInterpolation, FImageGradients.Mode orientationMode,
			SpatialBinningStrategy strategy)
	{
		this.extractor = new GradientOrientationHistogramExtractor(nbins, histogramInterpolation, orientationMode);

		this.strategy = strategy;
	}

	@Override
	public void analyseImage(FImage image) {
		extractor.analyseImage(image);
	}

	/**
	 * Analyse the given image, but construct the internal data such that the
	 * gradient magnitudes are multiplied by the given edge map before being
	 * accumulated. This could be used to suppress all magnitudes except those
	 * at edges; the resultant extracted histograms would only contain
	 * information about edge gradients.
	 *
	 * @param image
	 *            the image to analyse
	 * @param edges
	 *            the edge image
	 */
	public void analyseImage(FImage image, FImage edges) {
		extractor.analyseImage(image, edges);
	}

	/**
	 * Compute the HOG feature for the given window.
	 *
	 * @param rectangle
	 *            the window
	 * @return the computed HOG feature
	 */
	public Histogram getFeatureVector(Rectangle rectangle) {
		return currentHist = strategy.extract(extractor, rectangle, currentHist);
	}
}
