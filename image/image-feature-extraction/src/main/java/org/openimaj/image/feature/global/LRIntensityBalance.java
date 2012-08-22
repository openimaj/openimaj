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
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.pixel.statistics.BlockHistogramModel;
import org.openimaj.math.statistics.distribution.MultidimensionalHistogram;

/**
 * Implementation of the intensity balance algorithm described by Yeh et al.
 * <p>
 * The intensity balance measures how different the intensity is on the left
 * side of the image compared to the right. A balance of zero means exactly
 * balanced. Higher values are produced for more unbalanced images.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
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
public class LRIntensityBalance implements ImageAnalyser<FImage>, FeatureVectorProvider<DoubleFV> {
	int nbins = 64;
	double balance;

	/**
	 * Construct with the default 64 intensity histogram bins.
	 */
	public LRIntensityBalance() {
	}

	/**
	 * Construct with the given number of intensity histogram bins.
	 * 
	 * @param nbins
	 *            number of intensity histogram bins
	 */
	public LRIntensityBalance(int nbins) {
		this.nbins = nbins;
	}

	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(new double[] { balance });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.image.analyser.ImageAnalyser#analyseImage(org.openimaj.image
	 * .Image)
	 */
	@Override
	public void analyseImage(FImage image) {
		final BlockHistogramModel hm = new BlockHistogramModel(2, 1, nbins);

		hm.estimateModel(image);

		final MultidimensionalHistogram left = hm.histograms[0][0];
		final MultidimensionalHistogram right = hm.histograms[0][1];

		balance = left.compare(right, DoubleFVComparison.CHI_SQUARE);
	}
}
