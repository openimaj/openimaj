package org.openimaj.image.feature.validator;

import static org.junit.Assert.*;
import gnu.trove.TDoubleArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.interest.AbstractIPD.InterestPointData;
import org.openimaj.image.feature.local.interest.AffineIPD;
import org.openimaj.image.feature.local.interest.HarrisIPD;
import org.openimaj.image.feature.local.interest.HessianIPD;
import org.openimaj.image.feature.local.interest.InterestPointDetector;
import org.openimaj.image.feature.local.interest.InterestPointVisualiser;
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.shape.Triangle;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.pair.Pair;

import Jama.Matrix;

public class IPDRepeatabilityTest {
	private MBFImage image;
	private Ellipse shape;
	private Matrix transform;

	@Before
	public void setup(){
		// Create an Image
		image = new MBFImage(400,400,ColourSpace.RGB);
		image.fill(RGBColour.BLACK);
		// Create a shape
		shape = new Ellipse(100,100,20,10,Math.PI/2.0);
		// Create a transform
		transform = TransformUtilities.rotationMatrixAboutPoint(Math.PI/4, 100, 100);
	}
	
	@Test
	public void testRepeatability() throws IOException{
		MBFImage image2 = image.clone();
		image.drawShapeFilled(shape, RGBColour.WHITE);
		image2.drawShapeFilled(shape.transform(transform), RGBColour.WHITE);
		
		HarrisIPD internal = new HarrisIPD(1,5);
//		HessianIPD internal = new HessianIPD(1,5);
		InterestPointDetector ipd = new AffineIPD(internal,5);
		
		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(image));
		List<InterestPointData> interestPoints1 = ipd.getInterestPoints();
		
		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(image2));
		List<InterestPointData> interestPoints2 = ipd.getInterestPoints();
		
		InterestPointVisualiser<Float[],MBFImage> vis1 = InterestPointVisualiser.visualiseInterestPoints(image,interestPoints1);
		InterestPointVisualiser<Float[],MBFImage> vis2 = InterestPointVisualiser.visualiseInterestPoints(image2,interestPoints2);
		
		JFrame first = DisplayUtilities.display(vis1.drawPatches(RGBColour.RED, RGBColour.GREEN));
		JFrame second = DisplayUtilities.display(vis2.drawPatches(RGBColour.RED, RGBColour.GREEN));
		second.setBounds(400, 0, 400, 400);
		
		IPDRepeatability rep = new IPDRepeatability(image,image2,interestPoints1,interestPoints2,transform);
		double repeatability = rep.repeatability(0.5, 4);
		System.out.println("Repeatability: " + repeatability);
		assertTrue(repeatability == 1);
	}
	
	public static void main(String args[]) throws IOException{
		IPDRepeatabilityTest rep = new IPDRepeatabilityTest();
		rep.setup();
		rep.testRepeatability();
	}
}
