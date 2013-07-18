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

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.OpenIMAJ;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.convolution.FImageGradients;
import org.openimaj.image.processing.convolution.FImageGradients.Mode;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * Tests for {@link BinnedWindowedExtractor}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class SATWindowedExtractorTest {
	FImage image;
	SATWindowedExtractor satInterp;
	SATWindowedExtractor sat;
	InterpolatedBinnedWindowedExtractor binnedInterp;
	BinnedWindowedExtractor binned;
	private FImageGradients gradMags;

	/**
	 * Setup tests
	 * 
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException {
		image = ImageUtilities.readF(OpenIMAJ.getLogoAsStream());

		final Mode mode = FImageGradients.Mode.Unsigned;

		final FImage[] interpMags = new FImage[9];
		final FImage[] mags = new FImage[9];
		for (int i = 0; i < 9; i++) {
			interpMags[i] = new FImage(image.width, image.height);
			mags[i] = new FImage(image.width, image.height);
		}

		FImageGradients.gradientMagnitudesAndQuantisedOrientations(image, interpMags, true, mode);
		FImageGradients.gradientMagnitudesAndQuantisedOrientations(image, mags, false, mode);

		satInterp = new SATWindowedExtractor(interpMags);
		sat = new SATWindowedExtractor(mags);

		gradMags = FImageGradients.getGradientMagnitudesAndOrientations(image, mode);
		binnedInterp = new InterpolatedBinnedWindowedExtractor(9, mode.minAngle(), mode.maxAngle(), true);
		binned = new BinnedWindowedExtractor(9, mode.minAngle(), mode.maxAngle());
		gradMags.orientations.analyseWith(binnedInterp);
		gradMags.orientations.analyseWith(binned);
	}

	/**
	 * Test that the global histogram produced with {@link HistogramAnalyser}
	 * matches the one produced with {@link BinnedWindowedExtractor}.
	 */
	@Test
	public void testFullHistogram() {
		final Histogram hist1 = binned.computeHistogram(image.getBounds(), gradMags.magnitudes);
		final Histogram hist2 = sat.computeHistogram(image.getBounds());
		assertArrayEquals(hist1.values, hist2.values, 0.5);

		final Histogram hist3 = binnedInterp.computeHistogram(image.getBounds(), gradMags.magnitudes);
		final Histogram hist4 = satInterp.computeHistogram(image.getBounds());

		assertArrayEquals(hist3.values, hist4.values, 0.5);
	}
}
