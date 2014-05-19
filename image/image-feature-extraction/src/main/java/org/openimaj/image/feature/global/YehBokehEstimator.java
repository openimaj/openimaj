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
package org.openimaj.image.feature.global;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.processor.GridProcessor;
import org.openimaj.math.util.FloatArrayStatsUtils;

/**
 * Implementation of the Bokeh estimation feature described by Yeh et al.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Che-Hua Yeh", "Yuan-Chen Ho", "Brian A. Barsky", "Ming Ouhyoung" },
		title = "Personalized Photograph Ranking and Selection System",
		year = "2010",
		booktitle = "Proceedings of ACM Multimedia",
		pages = { "211", "220" },
		month = "October",
		customData = { "location", "Florence, Italy" })
public class YehBokehEstimator implements ImageAnalyser<FImage>, FeatureVectorProvider<DoubleFV> {
	class Sharpness implements GridProcessor<Float, FImage> {
		SharpPixelProportion bpp = new SharpPixelProportion();

		@Override
		public int getHorizontalGridElements() {
			return nBlocksX;
		}

		@Override
		public int getVerticalGridElements() {
			return nBlocksY;
		}

		@Override
		public Float processGridElement(FImage patch) {
			patch.analyseWith(bpp);
			return (float) bpp.getBlurredPixelProportion();
		}
	}

	class GreyLevelVariance implements GridProcessor<Float, FImage> {
		@Override
		public int getHorizontalGridElements() {
			return nBlocksX;
		}

		@Override
		public int getVerticalGridElements() {
			return nBlocksY;
		}

		@Override
		public Float processGridElement(FImage patch) {
			return FloatArrayStatsUtils.var(patch.pixels);
		}
	}

	Sharpness sharpProcessor = new Sharpness();
	GreyLevelVariance varProcessor = new GreyLevelVariance();

	int nBlocksX = 5;
	int nBlocksY = 5;

	float varThreshold = 0.1f;
	float sharpnessThreshold = 0.5f;
	float lowerBound = 0.3f;
	float upperBound = 0.7f;

	double bokeh;

	/**
	 * Construct with defaults: 5x5 blocks, variance threshold of 0.1, sharpness
	 * threshold of 0.5, lower bound of 0.3, upper bound of 0.7
	 */
	public YehBokehEstimator() {
	}

	/**
	 * Construct with the given parameters.
	 * 
	 * @param nBlocksX
	 *            number of blocks in the x-direction
	 * @param nBlocksY
	 *            number of blocks in the y-direction
	 * @param varThreshold
	 *            threshold for the variance
	 * @param sharpnessThreshold
	 *            threshold for the sharpness
	 * @param lowerBound
	 *            lower bound on Qbokeh for bokeh to be detected
	 * @param upperBound
	 *            upper bound on Qbokeh for bokeh to be detected
	 */
	public YehBokehEstimator(int nBlocksX, int nBlocksY, float varThreshold, float sharpnessThreshold, float lowerBound,
			float upperBound)
	{
		this.nBlocksX = nBlocksX;
		this.nBlocksY = nBlocksY;
		this.varThreshold = varThreshold;
		this.sharpnessThreshold = sharpnessThreshold;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(new double[] { bokeh });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj
	 * .image.Image)
	 */
	@Override
	public void analyseImage(FImage image) {
		final FImage sharpness = image.process(sharpProcessor);
		final FImage variance = image.process(varProcessor);

		double Qbokeh = 0;
		int validBlocks = 0;
		for (int y = 0; y < sharpness.height; y++) {
			for (int x = 0; x < sharpness.width; x++) {
				if (variance.pixels[y][x] >= varThreshold) {
					Qbokeh += sharpness.pixels[y][x] > 0.5 ? 1 : 0;
					validBlocks++;
				}
			}
		}
		Qbokeh /= (validBlocks);

		bokeh = (Qbokeh >= lowerBound && Qbokeh <= upperBound) ? 1 : 0;
	}
}
