package org.openimaj.demos;

import java.io.File;
import java.io.IOException;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.consistent.ConsistentKeypointMatcher;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processing.transform.ProjectionProcessor;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.transforms.HomographyModel;
import org.openimaj.math.model.fit.RANSAC;

public class SimpleMosaic {
	public static void main(String args[]) throws IOException{
//		ResizeProcessor rp = new ResizeProcessor(1920,1200);
//		ResizeProcessor rp = new ResizeProcessor(800,600);
		ResizeProcessor rp = new ResizeProcessor(1024,768);
		DoGSIFTEngine engine = new DoGSIFTEngine();

//		MBFImage imageMiddle = ImageUtilities.readMBF(new File("/Users/ss/Desktop/middle.jpg"));
		MBFImage imageMiddle = ImageUtilities.readMBF(new File("data/trento-view-1.jpg"));
		imageMiddle.processInline(rp);
		FImage workingImageMiddle = Transforms.calculateIntensityNTSC(imageMiddle);
		LocalFeatureList<Keypoint> middleKP = engine.findFeatures(workingImageMiddle);
		
		ConsistentKeypointMatcher<Keypoint> matcher = new ConsistentKeypointMatcher<Keypoint>(8, 0);
		HomographyModel model = new HomographyModel(6);
		RANSAC<Point2d,Point2d> modelFitting = new RANSAC<Point2d,Point2d>(model, 1600, new RANSAC.BestFitStoppingCondition(), true);
		matcher.setFittingModel(modelFitting);
		matcher.setModelFeatures(middleKP);
		ProjectionProcessor<Float[],MBFImage> ptp = new ProjectionProcessor<Float[],MBFImage>();
		imageMiddle.process(ptp);
		
		
//		MBFImage imageRight = ImageUtilities.readMBF(new File("/Users/ss/Desktop/right.jpg"));
		MBFImage imageRight = ImageUtilities.readMBF(new File("data/trento-view-0.jpg"));
		imageRight.processInline(rp);
		FImage workingImageRight = Transforms.calculateIntensityNTSC(imageRight);
		LocalFeatureList<Keypoint> rightKP = engine.findFeatures(workingImageRight);
		matcher.findMatches(rightKP);
		ptp.setMatrix(model.getTransform());
		imageRight.process(ptp);
		
//		MBFImage imageLeft = ImageUtilities.readMBF(new File("/Users/ss/Desktop/left.jpg"));
		MBFImage imageLeft = ImageUtilities.readMBF(new File("data/trento-view-2.jpg"));
		imageLeft.processInline(rp);
		FImage workingImageLeft= Transforms.calculateIntensityNTSC(imageLeft);
		LocalFeatureList<Keypoint> leftKP = engine.findFeatures(workingImageLeft);
		matcher.findMatches(leftKP);
		ptp.setMatrix(model.getTransform());
		imageLeft.process(ptp);
		
		MBFImage projected = ptp.performBlendedBackProjection(
				(int)(-imageMiddle.getWidth()/2.0),
				(int)(imageMiddle.getWidth() + imageMiddle.getWidth()/2.0),
				0,imageMiddle.getHeight(),(Float[])null);
		DisplayUtilities.display(projected);
		ImageUtilities.write(projected, "png", new File("/Users/ss/Desktop/mosaic.png"));
	}
}
