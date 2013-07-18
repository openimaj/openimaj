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

import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.analysis.algorithm.histogram.SATWindowedExtractor;
import org.openimaj.image.pixel.sampling.QuadtreeSampler;
import org.openimaj.image.processing.convolution.FImageGradients;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * This class is an implementation of an extractor for the PHOG (Pyramid
 * Histograms of Orientation Gradients) feature described by Bosch et al. The
 * PHOG feature is computed by creating a quadtree of orientation histograms
 * over the entire image and appending the histograms for each cell of the
 * quadtree into a single vector which is then l1 normalised (sum to unity). In
 * the original description, only orientations at edge pixels were counted; that
 * restriction is optional in this implementation.
 * 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class PHOG implements ImageAnalyser<FImage>, FeatureVectorProvider<DoubleFV> {
	private int nlevels = 3;
	private int nbins = 10;
	private FImageGradients.Mode orientationMode = FImageGradients.Mode.Unsigned;
	private ImageProcessor<FImage> edgeDetector;

	private SATWindowedExtractor histExtractor;
	private Rectangle lastBounds;

	@Override
	public void analyseImage(FImage image) {
		lastBounds = image.getBounds();

		final FImage[] magnitudes = new FImage[nbins];

		for (int i = 0; i < nbins; i++)
			magnitudes[i] = new FImage(image.width, image.height);

		FImageGradients.gradientMagnitudesAndQuantisedOrientations(image, magnitudes, true,
				orientationMode);

		if (edgeDetector != null) {
			final FImage edges = image.process(edgeDetector);

			for (int i = 0; i < nbins; i++)
				magnitudes[i].multiplyInplace(edges);
		}

		histExtractor = new SATWindowedExtractor(magnitudes);
	}

	public Histogram extractFeature() {
		return extractFeature(lastBounds);
	}

	public Histogram extractFeature(Rectangle rect) {
		final QuadtreeSampler sampler = new QuadtreeSampler(rect, nlevels);
		final List<float[]> parts = new ArrayList<float[]>();
		final Histogram hist = new Histogram(0);

		for (final Rectangle r : sampler) {
			hist.combine(histExtractor.computeHistogram(r));
		}

		return hist;
	}

	@Override
	public Histogram getFeatureVector() {
		return extractFeature();
	}
}
