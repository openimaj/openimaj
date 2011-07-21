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
package org.openimaj.image.feature.local.engine.ipd;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.DisplayUtilities;
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
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;
import org.openimaj.math.geometry.shape.Ellipse;

/**
 * Tests for the a SIFT keypoint engine which finds features using interest point detectors {@link InterestPointDetector}
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class IPDEngineTest {
	private MBFImage image;
	private Ellipse ellipseDrawn;
	private IPDSIFTEngine engine;

	/**
	 * create the test images, draw a few ellipses on the test image, initialise the IPDEngine
	 */
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
	
	/**
	 * Find the features and draw their locations
	 */
	@Test public void testEllipseFound(){
		LocalFeatureList<InterestPointKeypoint> features = engine.findFeatures(Transforms.calculateIntensityNTSC(image));
//		drawFeatures(features);
	}

	private void drawFeatures(LocalFeatureList<InterestPointKeypoint> features) {
		InterestPointVisualiser<Float[],MBFImage> ipv = InterestPointVisualiser.visualiseKeypoints(image, features);
		DisplayUtilities.display(ipv.drawPatches(RGBColour.RED, RGBColour.GREEN));
	}
	
	/**
	 * Run the IPD tests
	 * @param args
	 */
	public static void main(String args[]){
		IPDEngineTest test = new IPDEngineTest();
		test.setup();
		test.testEllipseFound();
	}
}
