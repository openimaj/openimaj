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
package org.openimaj.image.analysis.algorithm.histogram;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.analysis.algorithm.histogram.binning.SpatialBinningStrategy;
import org.openimaj.image.processing.convolution.FImageGradients;

/**
 * Implementation of the {@link WindowedHistogramExtractor} for efficiently
 * extracting gradient orientation histograms. This implementation is built on
 * top of a {@link SATWindowedExtractor}. The {@link #analyseImage(FImage)}
 * method can be used to precompute the underlying data required for efficient
 * histogram extraction using any of the <code>computeHistogram</code> methods.
 * <p>
 * Computed histograms are simply the sum of magnitudes for each orientation bin
 * over the given window. If you need to generate more complex features (for
 * example, aggregated spatially binned histograms) then use this class in
 * combination with a {@link SpatialBinningStrategy}.
 * <p>
 * The {@link #analyseImage(FImage, FImage)} method can be used to construct
 * histograms with moderated magnitudes (for example, suppressing all magnitudes
 * except those at edges).
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class GradientOrientationHistogramExtractor
extends SATWindowedExtractor
implements ImageAnalyser<FImage>
{
	private FImageGradients.Mode orientationMode;
	private boolean histogramInterpolation;

	/**
	 * Construct a new {@link GradientOrientationHistogramExtractor} with the
	 * given number of bins. Optionally perform linear interpolation across
	 * orientation bins. Histograms can also use either signed or unsigned
	 * gradients.
	 *
	 * @param nbins
	 *            number of bins
	 * @param histogramInterpolation
	 *            if true cyclic linear interpolation is used to share the
	 *            magnitude across the two closest bins; if false only the
	 *            closest bin will be filled.
	 * @param orientationMode
	 *            the range of orientations to extract
	 */
	public GradientOrientationHistogramExtractor(int nbins, boolean histogramInterpolation,
			FImageGradients.Mode orientationMode)
	{
		super(nbins);

		this.histogramInterpolation = histogramInterpolation;
		this.orientationMode = orientationMode;
	}

	@Override
	public void analyseImage(FImage image) {
		final FImage[] magnitudes = new FImage[nbins];

		for (int i = 0; i < nbins; i++)
			magnitudes[i] = new FImage(image.width, image.height);

		FImageGradients.gradientMagnitudesAndQuantisedOrientations(image, magnitudes, histogramInterpolation,
				orientationMode);

		computeSATs(magnitudes);
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
		final FImage[] magnitudes = new FImage[nbins];

		for (int i = 0; i < nbins; i++)
			magnitudes[i] = new FImage(image.width, image.height);

		FImageGradients.gradientMagnitudesAndQuantisedOrientations(image, magnitudes, histogramInterpolation,
				orientationMode);

		for (int i = 0; i < nbins; i++)
			magnitudes[i].multiplyInplace(edges);

		computeSATs(magnitudes);
	}
}
