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
package org.openimaj.image.feature.dense.gradient.dsift;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;
import org.openimaj.OpenIMAJ;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;

/**
 * Tests for {@link ColourDenseSIFT}
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ColourDenseSIFTTest {
	/**
	 * Test with the opponent colour space
	 * 
	 * @throws IOException
	 */
	@Test
	public void testOpponent() throws IOException {
		final MBFImage img = ImageUtilities.readMBF(OpenIMAJ.getLogoAsStream());

		final ColourDenseSIFT cdsift = new ColourDenseSIFT(new DenseSIFT(), ColourSpace.MODIFIED_OPPONENT);
		final DenseSIFT luminance_dsift = new DenseSIFT();
		final DenseSIFT o1_dsift = new DenseSIFT();
		final DenseSIFT o2_dsift = new DenseSIFT();

		final MBFImage oppImg = ColourSpace.MODIFIED_OPPONENT.convertFromRGB(img);

		cdsift.analyseImage(img);
		luminance_dsift.analyseImage(oppImg.getBand(0));
		o1_dsift.analyseImage(oppImg.getBand(1));
		o2_dsift.analyseImage(oppImg.getBand(2));

		assertEquals(cdsift.descriptors.length, luminance_dsift.descriptors.length);
		assertEquals(cdsift.descriptors.length, o1_dsift.descriptors.length);
		assertEquals(cdsift.descriptors.length, o2_dsift.descriptors.length);

		assertEquals(cdsift.descriptors[0].length, 3 * luminance_dsift.descriptors[0].length);

		final LocalFeatureList<ByteDSIFTKeypoint> cdbyte = cdsift.getByteKeypoints();
		final LocalFeatureList<ByteDSIFTKeypoint> ldbyte = luminance_dsift.getByteKeypoints();
		final LocalFeatureList<ByteDSIFTKeypoint> o1dbyte = o1_dsift.getByteKeypoints();
		final LocalFeatureList<ByteDSIFTKeypoint> o2dbyte = o2_dsift.getByteKeypoints();

		final LocalFeatureList<FloatDSIFTKeypoint> cdfloat = cdsift.getFloatKeypoints();
		final LocalFeatureList<FloatDSIFTKeypoint> ldfloat = luminance_dsift.getFloatKeypoints();
		final LocalFeatureList<FloatDSIFTKeypoint> o1dfloat = o1_dsift.getFloatKeypoints();
		final LocalFeatureList<FloatDSIFTKeypoint> o2dfloat = o2_dsift.getFloatKeypoints();

		for (int i = 0; i < cdsift.descriptors.length; i++) {
			assertArrayEquals(luminance_dsift.descriptors[i], ArrayUtils.subarray(cdsift.descriptors[i], 0, 128), 0f);
			assertArrayEquals(o1_dsift.descriptors[i], ArrayUtils.subarray(cdsift.descriptors[i], 128, 256), 0f);
			assertArrayEquals(o2_dsift.descriptors[i], ArrayUtils.subarray(cdsift.descriptors[i], 256, 384), 0f);

			assertEquals(cdbyte.get(i).x, ldbyte.get(i).x, 0);
			assertEquals(cdbyte.get(i).y, ldbyte.get(i).y, 0);
			assertEquals(cdbyte.get(i).energy, ldbyte.get(i).energy, 0);
			assertArrayEquals(ArrayUtils.subarray(cdbyte.get(i).descriptor, 0, 128), ldbyte.get(i).descriptor);
			assertArrayEquals(ArrayUtils.subarray(cdbyte.get(i).descriptor, 128, 256), o1dbyte.get(i).descriptor);
			assertArrayEquals(ArrayUtils.subarray(cdbyte.get(i).descriptor, 256, 384), o2dbyte.get(i).descriptor);

			assertEquals(cdfloat.get(i).x, ldfloat.get(i).x, 0);
			assertEquals(cdfloat.get(i).y, ldfloat.get(i).y, 0);
			assertEquals(cdfloat.get(i).energy, ldfloat.get(i).energy, 0);
			assertArrayEquals(ArrayUtils.subarray(cdfloat.get(i).descriptor, 0, 128), ldfloat.get(i).descriptor, 0f);
			assertArrayEquals(ArrayUtils.subarray(cdfloat.get(i).descriptor, 128, 256), o1dfloat.get(i).descriptor, 0f);
			assertArrayEquals(ArrayUtils.subarray(cdfloat.get(i).descriptor, 256, 384), o2dfloat.get(i).descriptor, 0f);
		}
	}

	/**
	 * Test with the opponent colour space
	 * 
	 * @throws IOException
	 */
	@Test
	public void testOpponentFast() throws IOException {
		final MBFImage img = ImageUtilities.readMBF(OpenIMAJ.getLogoAsStream());

		final ColourDenseSIFT cdsift = new ColourDenseSIFT(new ApproximateDenseSIFT(), ColourSpace.MODIFIED_OPPONENT);
		final DenseSIFT luminance_dsift = new ApproximateDenseSIFT();
		final DenseSIFT o1_dsift = new ApproximateDenseSIFT();
		final DenseSIFT o2_dsift = new ApproximateDenseSIFT();

		final MBFImage oppImg = ColourSpace.MODIFIED_OPPONENT.convertFromRGB(img);

		cdsift.analyseImage(img);
		luminance_dsift.analyseImage(oppImg.getBand(0));
		o1_dsift.analyseImage(oppImg.getBand(1));
		o2_dsift.analyseImage(oppImg.getBand(2));

		assertEquals(cdsift.descriptors.length, luminance_dsift.descriptors.length);
		assertEquals(cdsift.descriptors.length, o1_dsift.descriptors.length);
		assertEquals(cdsift.descriptors.length, o2_dsift.descriptors.length);

		assertEquals(cdsift.descriptors[0].length, 3 * luminance_dsift.descriptors[0].length);

		final LocalFeatureList<ByteDSIFTKeypoint> cdbyte = cdsift.getByteKeypoints();
		final LocalFeatureList<ByteDSIFTKeypoint> ldbyte = luminance_dsift.getByteKeypoints();
		final LocalFeatureList<ByteDSIFTKeypoint> o1dbyte = o1_dsift.getByteKeypoints();
		final LocalFeatureList<ByteDSIFTKeypoint> o2dbyte = o2_dsift.getByteKeypoints();

		final LocalFeatureList<FloatDSIFTKeypoint> cdfloat = cdsift.getFloatKeypoints();
		final LocalFeatureList<FloatDSIFTKeypoint> ldfloat = luminance_dsift.getFloatKeypoints();
		final LocalFeatureList<FloatDSIFTKeypoint> o1dfloat = o1_dsift.getFloatKeypoints();
		final LocalFeatureList<FloatDSIFTKeypoint> o2dfloat = o2_dsift.getFloatKeypoints();

		for (int i = 0; i < cdsift.descriptors.length; i++) {
			assertArrayEquals(luminance_dsift.descriptors[i], ArrayUtils.subarray(cdsift.descriptors[i], 0, 128), 0f);
			assertArrayEquals(o1_dsift.descriptors[i], ArrayUtils.subarray(cdsift.descriptors[i], 128, 256), 0f);
			assertArrayEquals(o2_dsift.descriptors[i], ArrayUtils.subarray(cdsift.descriptors[i], 256, 384), 0f);

			assertEquals(cdbyte.get(i).x, ldbyte.get(i).x, 0);
			assertEquals(cdbyte.get(i).y, ldbyte.get(i).y, 0);
			assertEquals(cdbyte.get(i).energy, ldbyte.get(i).energy, 0);
			assertArrayEquals(ArrayUtils.subarray(cdbyte.get(i).descriptor, 0, 128), ldbyte.get(i).descriptor);
			assertArrayEquals(ArrayUtils.subarray(cdbyte.get(i).descriptor, 128, 256), o1dbyte.get(i).descriptor);
			assertArrayEquals(ArrayUtils.subarray(cdbyte.get(i).descriptor, 256, 384), o2dbyte.get(i).descriptor);

			assertEquals(cdfloat.get(i).x, ldfloat.get(i).x, 0);
			assertEquals(cdfloat.get(i).y, ldfloat.get(i).y, 0);
			assertEquals(cdfloat.get(i).energy, ldfloat.get(i).energy, 0);
			assertArrayEquals(ArrayUtils.subarray(cdfloat.get(i).descriptor, 0, 128), ldfloat.get(i).descriptor, 0f);
			assertArrayEquals(ArrayUtils.subarray(cdfloat.get(i).descriptor, 128, 256), o1dfloat.get(i).descriptor, 0f);
			assertArrayEquals(ArrayUtils.subarray(cdfloat.get(i).descriptor, 256, 384), o2dfloat.get(i).descriptor, 0f);
		}
	}
}
