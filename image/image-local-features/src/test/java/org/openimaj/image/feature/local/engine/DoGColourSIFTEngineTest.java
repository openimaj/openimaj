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


public class DoGColourSIFTEngineTest {
	MBFImage im1;
	MBFImage im2;
	MBFImage im3;
	
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
