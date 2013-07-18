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

import org.junit.Test;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.histogram.InterpolatedBinnedWindowedExtractor;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * Tests for {@link BinnedWindowedExtractor}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class InterpolatingBinnedWindowedExtractorTest {
	/**
	 * Test with a single pixel falling into a single bin
	 */
	@Test
	public void testSinglePixel1() {
		final FImage i = new FImage(new float[][] { { 0.5f } });

		final InterpolatedBinnedWindowedExtractor a = new InterpolatedBinnedWindowedExtractor(5);
		a.analyseImage(i);

		final Histogram h = a.computeHistogram(0, 0, 1, 1);
		assertArrayEquals(new double[] { 0, 0, 1, 0, 0 }, h.values, 0.001);
	}

	/**
	 * Test with a single pixel falling into two bins equally
	 */
	@Test
	public void testSinglePixel2() {
		final FImage i = new FImage(new float[][] { { 0.5f } });

		final InterpolatedBinnedWindowedExtractor a = new InterpolatedBinnedWindowedExtractor(4);
		a.analyseImage(i);

		final Histogram h = a.computeHistogram(0, 0, 1, 1);
		assertArrayEquals(new double[] { 0, 0.5, 0.5, 0 }, h.values, 0.001);
	}

	/**
	 * Test with a single pixel falling into a single bin at the beginning
	 */
	@Test
	public void testSinglePixel3() {
		final FImage i = new FImage(new float[][] { { 0.0f } });

		final InterpolatedBinnedWindowedExtractor a = new InterpolatedBinnedWindowedExtractor(4);
		a.analyseImage(i);

		final Histogram h = a.computeHistogram(0, 0, 1, 1);
		assertArrayEquals(new double[] { 1, 0, 0, 0 }, h.values, 0.001);
	}

	/**
	 * Test with a single pixel falling into a single bin at the end
	 */
	@Test
	public void testSinglePixel4() {
		final FImage i = new FImage(new float[][] { { 1.0f } });

		final InterpolatedBinnedWindowedExtractor a = new InterpolatedBinnedWindowedExtractor(4);
		a.analyseImage(i);

		final Histogram h = a.computeHistogram(0, 0, 1, 1);
		assertArrayEquals(new double[] { 0, 0, 0, 1 }, h.values, 0.001);
	}

	/**
	 * Test with a single pixel falling between the end and beginning of a
	 * cyclic histogram
	 */
	@Test
	public void testSinglePixelWrap() {
		final FImage i = new FImage(new float[][] { { 1.0f } });

		final InterpolatedBinnedWindowedExtractor a = new InterpolatedBinnedWindowedExtractor(4);
		a.wrap = true;
		a.analyseImage(i);

		final Histogram h = a.computeHistogram(0, 0, 1, 1);
		assertArrayEquals(new double[] { 1.0, 0, 0, 0 }, h.values, 0.001);
	}

	/**
	 * Test with a single pixel falling into a single bin of a cyclic histogram
	 */
	@Test
	public void testSinglePixelWrap2() {
		final FImage i = new FImage(new float[][] { { 0.9f } });

		final InterpolatedBinnedWindowedExtractor a = new InterpolatedBinnedWindowedExtractor(5);
		a.wrap = true;
		a.analyseImage(i);

		final Histogram h = a.computeHistogram(0, 0, 1, 1);
		assertArrayEquals(new double[] { 0.5, 0, 0, 0, 0.5 }, h.values, 0.001);
	}

	/**
	 * Test with a single pixel falling between the end and beginning of a
	 * cyclic histogram
	 */
	@Test
	public void testSinglePixelWrap3() {
		final FImage i = new FImage(new float[][] { { 0.95f } });

		final InterpolatedBinnedWindowedExtractor a = new InterpolatedBinnedWindowedExtractor(5);
		a.wrap = true;
		a.analyseImage(i);

		final Histogram h = a.computeHistogram(0, 0, 1, 1);
		assertArrayEquals(new double[] { 0.75, 0, 0, 0, 0.25 }, h.values, 0.001);
	}
}
