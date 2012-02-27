package org.openimaj.demos.sandbox;

import java.io.File;
import java.io.IOException;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.engine.asift.ASIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.transforms.AffineTransformModel;
import org.openimaj.math.model.fit.RANSAC;

public class SiftVsAsift {
	public static void main(String[] args) throws IOException {
		FImage image1 = ImageUtilities.readF(new File("/Users/jsh2/Desktop/1.JPG"));
		FImage image2 = ImageUtilities.readF(new File("/Users/jsh2/Desktop/2.JPG"));
		
		image1 = ResizeProcessor.halfSize(image1);
		image2 = ResizeProcessor.halfSize(image2);
		
		DoGSIFTEngine sift = new DoGSIFTEngine();
		sift.getOptions().setDoubleInitialImage(false);
		LocalFeatureList<Keypoint> sift1 = sift.findFeatures(image1);
		LocalFeatureList<Keypoint> sift2 = sift.findFeatures(image2);
		
		FastBasicKeypointMatcher<Keypoint> innermatcher = new FastBasicKeypointMatcher<Keypoint>(8);
		ConsistentLocalFeatureMatcher2d<Keypoint> matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(innermatcher);
		matcher.setFittingModel(new RANSAC<Point2d, Point2d>(new AffineTransformModel(5), 1000, new RANSAC.BestFitStoppingCondition(), true));
		
		matcher.setModelFeatures(sift1);
		matcher.findMatches(sift2);
		//DisplayUtilities.display(MatchingUtilities.drawMatches(image1, image2, matcher.getMatches(), 1f));
		
		ImageUtilities.write(MatchingUtilities.drawMatches(new MBFImage(image1,image1,image1), new MBFImage(image2,image2,image2), matcher.getAllMatches(), RGBColour.RED), new File("/Users/jsh2/Desktop/matches-sift.png"));
		
		ASIFTEngine asift = new ASIFTEngine();
		LocalFeatureList<Keypoint> asift1 = asift.findKeypoints(image1);
		LocalFeatureList<Keypoint> asift2 = asift.findKeypoints(image2);
		
		matcher.setModelFeatures(asift1);
		matcher.findMatches(asift2);
		
//		DisplayUtilities.display(MatchingUtilities.drawMatches(image1, image2, matcher.getMatches(), 1f));
		ImageUtilities.write(MatchingUtilities.drawMatches(new MBFImage(image1,image1,image1), new MBFImage(image2,image2,image2), matcher.getAllMatches(), RGBColour.RED, matcher.getMatches(), RGBColour.BLUE), new File("/Users/jsh2/Desktop/matches-asift.png"));
		
	}
}
