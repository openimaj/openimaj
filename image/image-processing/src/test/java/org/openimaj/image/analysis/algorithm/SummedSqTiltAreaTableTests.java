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
package org.openimaj.image.analysis.algorithm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openimaj.data.RandomData;
import org.openimaj.image.FImage;

/**
 * Tests for {@link SummedSqTiltAreaTable}s.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class SummedSqTiltAreaTableTests {
	/**
	 * Compare all SAT implementations to ensure consistent results.
	 */
	@Test
	public void testCompare() {
		final int sz = 100;
		final FImage image = new FImage(RandomData.getRandomFloatArray(sz - 1, sz - 1, 0f, 1f));

		final SummedAreaTable sat = new SummedAreaTable(image);
		final SummedSqAreaTable sqsat = new SummedSqAreaTable(image);
		final SummedSqTiltAreaTable sqsat2 = new SummedSqTiltAreaTable(image, false);
		final SummedSqTiltAreaTable sqtsat = new SummedSqTiltAreaTable(image, true);

		assertEquals(sz, sat.data.width);
		assertEquals(sz, sat.data.height);

		assertEquals(sz, sqsat.sum.width);
		assertEquals(sz, sqsat.sum.height);
		assertEquals(sz, sqsat.sqSum.width);
		assertEquals(sz, sqsat.sqSum.height);

		assertEquals(sz, sqsat2.sum.width);
		assertEquals(sz, sqsat2.sum.height);
		assertEquals(sz, sqsat2.sqSum.width);
		assertEquals(sz, sqsat2.sqSum.height);
		assertEquals(null, sqsat2.tiltSum);

		assertEquals(sz, sqtsat.sum.width);
		assertEquals(sz, sqtsat.sum.height);
		assertEquals(sz, sqtsat.sqSum.width);
		assertEquals(sz, sqtsat.sqSum.height);
		assertEquals(sz, sqtsat.tiltSum.width);
		assertEquals(sz, sqtsat.tiltSum.height);

		for (int y = 0; y < sz; y++) {
			for (int x = 0; x < sz; x++) {
				assertEquals(sat.data.pixels[y][x], sqsat.sum.pixels[y][x], 0.1f);
				assertEquals(sat.data.pixels[y][x], sqsat2.sum.pixels[y][x], 0.1f);
				assertEquals(sat.data.pixels[y][x], sqtsat.sum.pixels[y][x], 0.1f);

				assertEquals(sqsat.sqSum.pixels[y][x], sqsat2.sqSum.pixels[y][x], 0.1f);
				assertEquals(sqsat.sqSum.pixels[y][x], sqtsat.sqSum.pixels[y][x], 0.1f);
			}
		}
	}
}
