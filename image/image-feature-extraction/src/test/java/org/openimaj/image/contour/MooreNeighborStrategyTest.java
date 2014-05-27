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
package org.openimaj.image.contour;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;

/**
 * Tests for the {@link MooreNeighborStrategy}
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class MooreNeighborStrategyTest {

	/**
	 * @throws Exception
	 */
	@Test
	public void testSimple() throws Exception {
		final float[][] pixels = new float[][] {
				new float[] { 0, 0, 0, 0, 0, 0 },
				new float[] { 0, 0, 1, 1, 1, 0 },
				new float[] { 0, 1, 0, 0, 1, 0 },
				new float[] { 0, 0, 1, 1, 1, 0 },
				new float[] { 0, 0, 0, 0, 0, 0 }
		};

		final FImage img = new FImage(pixels);

		final ContourFollowingStrategy strat = new MooreNeighborStrategy();
		final Pixel start = new Pixel(1, 2);
		Pixel from = new Pixel(1, 3);
		List<Pixel> contour = strat.contour(img, start, from);
		assertTrue(contour.size() == img.sum());
		from = new Pixel(2, 2);
		contour = strat.contour(img, start, from);
		assertTrue(contour.size() == img.sum() - 2);

	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testOpenLoop() throws Exception {
		final float[][] pixels = new float[][] {
				new float[] { 0, 0, 0, 0, 0, 0, 0, 0 },
				new float[] { 0, 0, 0, 0, 0, 0, 0, 0 },
				new float[] { 0, 0, 0, 0, 0, 1, 1, 0 },
				new float[] { 0, 0, 0, 1, 0, 0, 1, 0 },
				new float[] { 0, 0, 0, 1, 0, 1, 1, 0 },
				new float[] { 0, 0, 0, 0, 1, 1, 0, 0 },
				new float[] { 0, 0, 0, 0, 0, 0, 0, 0 }
		};

		final FImage img = new FImage(pixels);

		final MooreNeighborStrategy strat = new MooreNeighborStrategy();
		final Pixel start = new Pixel(3, 4);
		final Pixel from = new Pixel(3, 5);
		strat.contour(img, start, from);
	}

}
