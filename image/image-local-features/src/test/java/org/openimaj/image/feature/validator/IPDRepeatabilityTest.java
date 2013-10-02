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
package org.openimaj.image.feature.validator;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.interest.AffineAdaption;
import org.openimaj.image.feature.local.interest.EllipticInterestPointData;
import org.openimaj.image.feature.local.interest.HarrisIPD;
import org.openimaj.image.feature.local.interest.IPDSelectionMode;
import org.openimaj.image.feature.local.interest.InterestPointVisualiser;
import org.openimaj.image.feature.local.interest.experiment.IPDRepeatability;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.pair.Pair;

import Jama.Matrix;

/**
 * Test the IPD Repeatability class
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class IPDRepeatabilityTest {
	private MBFImage image;
	private Ellipse shape;
	private Matrix transform;

	/**
	 * Initialise the image and 
	 */
	@Before
	public void setup(){
		// Create an Image
		image = new MBFImage(400,400,ColourSpace.RGB);
		image.fill(RGBColour.BLACK);
		// Create a shape
		shape = new Ellipse(100,100,20,10,Math.PI/2.0);
		// Create a transform
		transform = TransformUtilities.rotationMatrixAboutPoint(0.01, 100, 100);
	}
	
	/**
	 * Test the repeatability by making sure all features found in one image are found in another.
	 * @throws IOException
	 */
	@Test
	public void testRepeatability() throws IOException{
		MBFImage image2 = image.clone();
		MBFImageRenderer renderer2 = image2.createRenderer();
		renderer2.drawShapeFilled(shape.transform(transform), RGBColour.WHITE);
		MBFImageRenderer renderer1 = image.createRenderer();
		renderer1.drawShapeFilled(shape, RGBColour.WHITE);
//		ImageUtilities.write(image, "png", new File("/Users/ss/Desktop/ellipse1.jpg"));
//		ImageUtilities.write(image2, "png", new File("/Users/ss/Desktop/ellipse2.jpg"));
		
//		DisplayUtilities.display(image);
//		DisplayUtilities.display(image2);
		
		HarrisIPD internal = new HarrisIPD(1,2,0.04f);
//		HessianIPD internal = new HessianIPD(1,5);
		AffineAdaption ipd = new AffineAdaption(internal,new IPDSelectionMode.All());
		ipd.setFastDifferentiationScale(true);
		
		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(image));
		List<EllipticInterestPointData> interestPoints1 = ipd.getInterestPoints();
		
		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(image2));
		List<EllipticInterestPointData> interestPoints2 = ipd.getInterestPoints();
		
//		InterestPointVisualiser<Float[],MBFImage> vis1 = InterestPointVisualiser.visualiseInterestPoints(image,interestPoints1);
//		InterestPointVisualiser<Float[],MBFImage> vis2 = InterestPointVisualiser.visualiseInterestPoints(image2,interestPoints2);
//		displayMatches(vis1, vis2);
		
		IPDRepeatability<EllipticInterestPointData> rep = IPDRepeatability.repeatability(image,image2,interestPoints1,interestPoints2,transform,4);
		double repeatability = rep.repeatability(0.5);
		System.out.println("Repeatability: " + repeatability);
		assertTrue(repeatability == 1);
	}
	
	
//	public void testOxfordRepeatability() throws IOException{
//		MBFImage image1 = ImageUtilities.readMBF(this.getClass().getResourceAsStream("/org/openimaj/image/feature/validator/graf/img1.ppm"));
//		MBFImage image2 = ImageUtilities.readMBF(this.getClass().getResourceAsStream("/org/openimaj/image/feature/validator/graf/img2.ppm"));
//		
//		Matrix transform = IPDRepeatability.readHomography(this.getClass().getResourceAsStream("/org/openimaj/image/feature/validator/graf/H1to2p"));
//		
//		List<Ellipse> img1Ellipses = IPDRepeatability.readMatlabInterestPoints(this.getClass().getResourceAsStream("/org/openimaj/image/feature/validator/graf/img1.haraff"));
//		List<Ellipse> img2Ellipses = IPDRepeatability.readMatlabInterestPoints(this.getClass().getResourceAsStream("/org/openimaj/image/feature/validator/graf/img2.haraff"));
//		
////		List<Ellipse> validImg2Ellipses = IPDRepeatability.validPoints(img2Ellipses, image1, transform);
////		Map<Pair<Ellipse>, Double> correspondingPoints = IPDRepeatability.calculateOverlappingEllipses(img1Ellipses, img2Ellipses, transform, 4);
////		
////		displayFeatures(image1, img1Ellipses);
////		displayFeatures(image2, img2Ellipses);
////		displayMatchingPoints(image1,image2,correspondingPoints);
//		
//		IPDRepeatability<EllipticInterestPointData> rep = IPDRepeatability.repeatability(image1,image2,img1Ellipses,img2Ellipses,transform,4);
//		System.out.println("error %: repeatability");
//		for(float perc = 0.9f; perc >= 0; perc-=0.1f){
//			float error = 1 - perc;
//			System.out.println(error + ": "+ rep.repeatability(perc));
//		}
//		
//	}
	
	protected void displayMatchingPoints(MBFImage image1, MBFImage image2,Map<Pair<Ellipse>, Double> correspondingPoints) {
		
	}

	protected void displayFeatures(MBFImage image1, List<Ellipse> img1Ellipses) {
		DisplayUtilities.display(new InterestPointVisualiser<Float[],MBFImage>(image1,img1Ellipses).drawPatches(RGBColour.BLUE, RGBColour.RED));
	}

	protected void displayMatches(InterestPointVisualiser<Float[], MBFImage> vis1, InterestPointVisualiser<Float[], MBFImage> vis2) {
		DisplayUtilities.display(vis1.drawPatches(RGBColour.RED, RGBColour.GREEN));
		JFrame second = DisplayUtilities.display(vis2.drawPatches(RGBColour.RED, RGBColour.GREEN));
		second.setBounds(400, 0, 400, 400);
	}

	/**
	 * Run tests as an application
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String args[]) throws IOException{
		IPDRepeatabilityTest rep = new IPDRepeatabilityTest();
		rep.setup();
//		rep.testOxfordRepeatability();
		rep.testRepeatability();
	}
}
