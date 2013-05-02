package org.openimaj.demos.sitestuff;

import java.io.File;
import java.io.IOException;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.HomographyModel;
import org.openimaj.math.model.fit.RANSAC;

import Jama.Matrix;

public class KeypointMatchSiteDemo {
	static File monaLisaSource = new File("/Users/ss/Desktop/Van-Gogh-Starry-Nights.jpg");
	static File monaLisaTarget = new File("/Users/ss/Desktop/4172349-A_SPECIAL_EXHIBITION_VAN_GOGHS_STARRY_NIGHT_New_Haven.jpg");

	public static void main(String[] args) throws IOException {
		MBFImage sourceC = ImageUtilities.readMBF(monaLisaSource);
		MBFImage targetC = ImageUtilities.readMBF(monaLisaTarget);
		FImage source = sourceC.flatten();
		FImage target = targetC.flatten();

		DoGSIFTEngine eng = new DoGSIFTEngine();

		LocalFeatureList<Keypoint> sourceFeats = eng.findFeatures(source);
		LocalFeatureList<Keypoint> targetFeats = eng.findFeatures(target);

		final HomographyModel model = new HomographyModel(5f);
		final RANSAC<Point2d, Point2d> ransac = new RANSAC<Point2d, Point2d>(model, 1500, new RANSAC.BestFitStoppingCondition(), true);
		ConsistentLocalFeatureMatcher2d<Keypoint> matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(new FastBasicKeypointMatcher<Keypoint>(8));
		matcher.setFittingModel(ransac);

		matcher.setModelFeatures(sourceFeats);
		matcher.findMatches(targetFeats);

		final Matrix boundsToPoly = model.getTransform().inverse();

		Shape s = source.getBounds().transform(boundsToPoly);
		targetC.drawShape(s, 10, RGBColour.BLUE);
		MBFImage matches = MatchingUtilities.drawMatches(sourceC, targetC, matcher.getMatches(), RGBColour.RED);

		matches.processInplace(new ResizeProcessor(640, 480));
		DisplayUtilities.display(matches);
		ImageUtilities.write(matches, new File("/Users/ss/Desktop/keypoint-match-example.png"));

	}
}
