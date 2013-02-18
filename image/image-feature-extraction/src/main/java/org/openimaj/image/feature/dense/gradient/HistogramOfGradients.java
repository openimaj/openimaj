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

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.analysis.algorithm.BinnedImageHistogramAnalyser;
import org.openimaj.image.processing.convolution.FImageGradients;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * Implementation of the Histogram of Gradients (HOG) feature. This
 * implementation allows any kind of spatial layout to be used, and provides the
 * standard Rectangular (R-HOG) and Circular (C-HOG) strategies. Features can be
 * efficiently extracted for the whole image, or sub-region(s).
 * <p>
 * The original description of HOG describes a feature that essentially
 * describes local TEXTURE based on the histogram of all gradients in the patch
 * (like dense SIFT). Confusingly, a different feature called PHOG (Pyramid HOG)
 * was later proposed that is primarily a SHAPE descriptor. PHOG computes
 * HOG-like descriptors in a spatial pyramid; however it only counts gradients
 * belonging to strong edges (hence it why describes shape rather than texture).
 * Both these descriptors obviously have their merits, but it is also likely
 * that a SHAPE variant of the HOG and a FEATURE variant of the PHOG could also
 * be useful. With this in mind, this class can optionally be used to compute a
 * modified HOG feature which suppresses gradients at certain spatial locations
 * (i.e. those not on edges).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class HistogramOfGradients implements ImageAnalyser<FImage> {
	public interface Strategy {
		public Histogram extract(BinnedImageHistogramAnalyser binData, FImage magnitudes, Rectangle region);
	}

	BinnedImageHistogramAnalyser histExtractor;
	Strategy strategy;
	FImage magnitudes;

	public HistogramOfGradients(BinnedImageHistogramAnalyser histExtractor, Strategy strategy) {
		this.histExtractor = histExtractor;
		this.strategy = strategy;
	}

	@Override
	public void analyseImage(FImage image) {
		final FImageGradients gm = FImageGradients.getGradientMagnitudesAndOrientations(image);
		this.analyse(gm.magnitudes, gm.orientations);
	}

	public void analyseImage(FImage image, FImage edges) {
		final FImageGradients gm = FImageGradients.getGradientMagnitudesAndOrientations(image);
		this.analyse(gm.magnitudes.multiplyInplace(edges), gm.orientations);
	}

	public void analyse(FImage magnitudes, FImage orientations) {
		histExtractor.analyseImage(orientations);
		this.magnitudes = magnitudes;
	}

	public Histogram extractFeature(Rectangle rectangle) {
		return strategy.extract(histExtractor, magnitudes, rectangle);
	}
}
