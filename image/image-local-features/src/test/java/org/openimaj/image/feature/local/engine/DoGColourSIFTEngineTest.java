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
package org.openimaj.image.feature.local.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.BasicMatcher;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.shape.Circle;

/**
 * Tests for {@link DoGColourSIFTEngine}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class DoGColourSIFTEngineTest {
	MBFImage im1;
	MBFImage im2;
	MBFImage im3;
	
	/**
	 * Constructor
	 */
	public DoGColourSIFTEngineTest() {
		FImage blankImage = new FImage(300,300);
		FImage testImage = new FImage(300,300);
		
		testImage.drawShapeFilled(new Circle(150 ,150, 50), 1f);
		
		im1 = new MBFImage(testImage, blankImage, blankImage);
		im2 = new MBFImage(blankImage, testImage, blankImage);
		im3 = new MBFImage(blankImage, blankImage, testImage);
	}
	
	private Keypoint getFirstKeypoint(MBFImage image) {
		return new DoGColourSIFTEngine().findFeatures(image).get(0);
	}
	
	/**
	 * Test feature extraction
	 */
	@Test
	public void testFeature() {
		Keypoint kpt1 = getFirstKeypoint(im1);
		Keypoint kpt2 = getFirstKeypoint(im2);
		Keypoint kpt3 = getFirstKeypoint(im3);
		
		for (int i=0; i<128*3; i++) {
			if (i>=128) assertTrue(kpt1.ivec[i] == -128);
			if (i>=2*128 || i<128) assertTrue(kpt2.ivec[i] == -128);
			if (i<2*128) assertTrue(kpt3.ivec[i] == -128);
		}
		
		for (int i=0; i<128;i++) {
			assertTrue(kpt1.ivec[i] == kpt2.ivec[i+128]);
			assertTrue(kpt1.ivec[i] == kpt3.ivec[i+256]);
		}
	}
	
	/**
	 * Test feature matching
	 */
	@Test
	public void testMatching() {
		DoGColourSIFTEngine engine = new DoGColourSIFTEngine();
		LocalFeatureList<Keypoint> kpts1 = engine.findFeatures(im1);
		LocalFeatureList<Keypoint> kpts2 = engine.findFeatures(im2);
		LocalFeatureList<Keypoint> kpts3 = engine.findFeatures(im3);
		
		//test features only match against themselves
		
		BasicMatcher<Keypoint> bm = new BasicMatcher<Keypoint>(8);
		bm.setModelFeatures(kpts1);
		bm.findMatches(kpts1);
		assertEquals(kpts1.size(), bm.getMatches().size());
		
		bm.findMatches(kpts2);
		assertEquals(0, bm.getMatches().size());
		
		bm.findMatches(kpts3);
		assertEquals(0, bm.getMatches().size());
		
	}
}
