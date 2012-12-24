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
package org.openimaj.image;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openimaj.image.colour.ColourSpace;

/**
 * Test some colour space conversion
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
 *         (ss@ecs.soton.ac.uk)
 */
public class ColourSpaceTest {
	/**
	 * @throws Exception
	 */
	@Test
	public void testRGBtoRGBA() throws Exception {
		final MBFImage img = ImageUtilities.readMBF(ColourSpaceTest.class
				.getResourceAsStream("/org/openimaj/image/data/sinaface.jpg"));
		final MBFImage imgConv = ColourSpace.convert(img, ColourSpace.RGBA);
		assertTrue(img.getBounds().equals(imgConv.getBounds()));
		assertTrue(img.equals(ColourSpace.convert(imgConv, ColourSpace.RGB)));
	}

	/**
	 * Test the 3 band conversions
	 */
	@Test
	public void test2way() {
		final double eps = 0.05;

		for (final ColourSpace cs : ColourSpace.values()) {
			if (cs.getNumBands() == 3 && cs != ColourSpace.RGB_INTENSITY_NORMALISED) {
				try {
					final MBFImage rgbIn = MBFImage.randomImage(100, 100);

					final MBFImage cvt = cs.convertFromRGB(rgbIn);
					final MBFImage rgbOut = cs.convertToRGB(cvt);

					for (int y = 0; y < 100; y++) {
						for (int x = 0; x < 100; x++) {
							final float rin = rgbIn.getBand(0).pixels[y][x];
							final float gin = rgbIn.getBand(1).pixels[y][x];
							final float bin = rgbIn.getBand(2).pixels[y][x];

							final float rout = rgbOut.getBand(0).pixels[y][x];
							final float gout = rgbOut.getBand(1).pixels[y][x];
							final float bout = rgbOut.getBand(2).pixels[y][x];

							assertTrue(Math.abs(rin - rout) < eps);
							assertTrue(Math.abs(gin - gout) < eps);
							assertTrue(Math.abs(bin - bout) < eps);
						}
					}
				} catch (final UnsupportedOperationException e) {
					// ignore
				}
			}
		}
	}
}
