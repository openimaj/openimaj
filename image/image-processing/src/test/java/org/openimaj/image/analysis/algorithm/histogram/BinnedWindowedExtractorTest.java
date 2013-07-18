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
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * Tests for {@link BinnedWindowedExtractor}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class BinnedWindowedExtractorTest {
	FImage image;
	BinnedWindowedExtractor analyser;

	/**
	 * Setup tests
	 * 
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException {
		image = ImageUtilities.readF(OpenIMAJ.getLogoAsStream());
		analyser = new BinnedWindowedExtractor(64);
		image.analyseWith(analyser);
	}

	/**
	 * Test that the global histogram produced with {@link HistogramAnalyser}
	 * matches the one produced with {@link BinnedWindowedExtractor}.
	 */
	@Test
	public void testFullHistogram() {
		final Histogram hist1 = HistogramAnalyser.getHistogram(image, analyser.nbins);
		final Histogram hist2 = analyser.computeHistogram(0, 0, image.width, image.height);

		assertArrayEquals(hist1.values, hist2.values, 0.0001);
	}

	/**
	 * Test that the windowed histogram produced with {@link HistogramAnalyser}
	 * matches the one produced with {@link BinnedWindowedExtractor}.
	 */
	@Test
	public void testWindowedHistogram() {
		final Rectangle roi = new Rectangle(50, 10, 100, 100);

		final Histogram hist1 = HistogramAnalyser.getHistogram(image.extractROI(roi), analyser.nbins);
		final Histogram hist2 = analyser.computeHistogram(roi);

		assertArrayEquals(hist1.values, hist2.values, 0.0001);
	}
}
