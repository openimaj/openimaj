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
package org.openimaj.image.processing.resize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.resize.filters.BSplineFilter;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Tests for the {@link ResizeProcessor}
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ResizeProcessorTest {
	/**
	 * Test bounded zoom
	 *
	 * @throws Exception
	 */
	@Test
	public void testBoundedZoom() throws Exception {
		final FImage image = ImageUtilities.readF(ResizeProcessorTest.class
				.getResourceAsStream("/org/openimaj/image/data/sinaface.jpg"));

		FImage imageOut = new FImage(200, 100);
		final ResizeFilterFunction filter = new BSplineFilter();
		final Rectangle inLoc = new Rectangle(30f, 30f, 100f, 100f);
		Rectangle outLoc = imageOut.getBounds();
		ResizeProcessor.zoom(
				image, inLoc,
				imageOut, outLoc,
				filter
				);

		imageOut = new FImage(20, 20);
		outLoc = imageOut.getBounds();
		ResizeProcessor.zoom(
				image, inLoc,
				imageOut, outLoc,
				filter
				);

		// Now some speed tests, 10000 times with extract and 10000 times with
		// new zoom
		// long start = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			ResizeProcessor.zoom(
					image, inLoc,
					imageOut, outLoc,
					filter
					);
		}
		// long end = System.currentTimeMillis();
		// System.out.println("Time taken (inplace zoom): " + (end - start));
		// start = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			final FImage imageExtracted = image.extractROI(inLoc); // Can't do
			// it any
			// other way
			// with
			// normal
			// zoom
			ResizeProcessor.zoom(
					imageExtracted,
					imageOut,
					filter
					);
		}
		// end = System.currentTimeMillis();
		// System.out.println("Time taken (extract/zoom): " + (end - start));
	}

	/**
	 * Test resizeMaxArea
	 */
	@Test
	public void testResizeMaxArea1() {
		FImage image = new FImage(100, 200);

		image = ResizeProcessor.resizeMaxArea(image, 5000);

		assertEquals(5000, image.height * image.width);
		assertEquals(100.0 / 200.0, (double) image.width / (double) image.height, 0.001);
	}

	/**
	 * Test resizeMaxArea
	 */
	@Test
	public void testResizeMaxArea2() {
		FImage image = new FImage(200, 100);

		image = ResizeProcessor.resizeMaxArea(image, 5000);

		assertEquals(5000, image.height * image.width);
		assertEquals(200.0 / 100.0, (double) image.width / (double) image.height, 0.001);
	}

	/**
	 * Test resizeMaxArea
	 */
	@Test
	public void testResizeMaxArea3() {
		FImage image = new FImage(2687, 3356);

		image = ResizeProcessor.resizeMaxArea(image, 3700000);

		assertTrue(3700000 > image.height * image.width);
		assertEquals(2687.0 / 3356.0, (double) image.width / (double) image.height, 0.001);
	}
}
