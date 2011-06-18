package org.openimaj.image.feature.local.engine.ipd;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.detector.ipd.finder.OctaveInterestPointFinder.FeatureMode;
import org.openimaj.image.feature.local.interest.AbstractIPD;
import org.openimaj.image.feature.local.interest.AffineIPD;
import org.openimaj.image.feature.local.interest.HarrisIPD;
import org.openimaj.image.feature.local.interest.InterestPointDetector;
import org.openimaj.image.feature.local.interest.InterestPointVisualiser;
import org.openimaj.image.feature.local.interest.AbstractIPD.InterestPointData;
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;
import org.openimaj.math.geometry.shape.Ellipse;

public class IPDEngineTest {
	private MBFImage image;
	private Ellipse ellipseDrawn;
	private IPDSIFTEngine engine;

	@Before public void setup(){
		image = new MBFImage(400,400,ColourSpace.RGB);
		ellipseDrawn = new Ellipse(200,200,100,50,Math.PI/4);
		
		image.fill(RGBColour.WHITE);
		image.drawShapeFilled(ellipseDrawn, RGBColour.BLACK);
		
		int derScale = 100;
		int intScale = derScale  * 3;
		InterestPointDetector ipd;
		AbstractIPD aipd = new HarrisIPD(derScale,intScale);
		AffineIPD affine = new AffineIPD(aipd,2);
		ipd = affine;
		engine = new IPDSIFTEngine(ipd);
		engine.setMode(FeatureMode.NUMBER);
		engine.setFeatureModeLevel(2);
		engine.setCollectorMode(IPDSIFTEngine.CollectorMode.AFFINE);
		engine.setAcrossScales(false);
	}
	
	@Test public void testEllipseFound(){
		LocalFeatureList<InterestPointKeypoint> features = engine.findFeatures(Transforms.calculateIntensityNTSC(image));
		drawFeatures(features);
	}

	private void drawFeatures(LocalFeatureList<InterestPointKeypoint> features) {
		InterestPointVisualiser<Float[],MBFImage> ipv = InterestPointVisualiser.visualiseKeypoints(image, features);
		DisplayUtilities.display(ipv.drawPatches(RGBColour.RED, RGBColour.GREEN));
	}
	
	public static void main(String args[]){
		IPDEngineTest test = new IPDEngineTest();
		test.setup();
		test.testEllipseFound();
	}
}
