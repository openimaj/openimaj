package org.openimaj.docs.tutorial.images.siftmatch;

import java.io.IOException;
import java.net.URL;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.BasicMatcher;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.transforms.HomographyModel;
import org.openimaj.math.model.fit.RANSAC;

/**
 * OpenIMAJ Hello world!
 * 
 */
public class App {
	/**
	 * Main method
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final MBFImage query = ImageUtilities.readMBF(new URL("http://dl.dropbox.com/u/8705593/query.jpg"));
		final MBFImage target = ImageUtilities.readMBF(new URL("http://dl.dropbox.com/u/8705593/target.jpg"));

		final DoGSIFTEngine engine = new DoGSIFTEngine();
		final LocalFeatureList<Keypoint> queryKeypoints = engine.findFeatures(query.flatten());
		final LocalFeatureList<Keypoint> targetKeypoints = engine.findFeatures(target.flatten());

		LocalFeatureMatcher<Keypoint> matcher = new BasicMatcher<Keypoint>(80);
		matcher.setModelFeatures(queryKeypoints);
		matcher.findMatches(targetKeypoints);

		final MBFImage basicMatches = MatchingUtilities.drawMatches(query, target, matcher.getMatches(), RGBColour.RED);
		DisplayUtilities.display(basicMatches);

		// final AffineTransformModel fittingModel = new
		// AffineTransformModel(5);
		final HomographyModel fittingModel = new HomographyModel(5);
		final RANSAC<Point2d, Point2d> ransac =
				new RANSAC<Point2d, Point2d>(fittingModel, 1500, new RANSAC.PercentageInliersStoppingCondition(0.5), true);
		matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
				new FastBasicKeypointMatcher<Keypoint>(8), ransac);
		matcher.setModelFeatures(queryKeypoints);
		matcher.findMatches(targetKeypoints);
		final MBFImage consistentMatches = MatchingUtilities.drawMatches(query, target,
				matcher.getMatches(), RGBColour.RED);
		DisplayUtilities.display(consistentMatches);

		target.drawShape(query.getBounds().transform(fittingModel.getTransform().inverse()), 3, RGBColour.BLUE);
		DisplayUtilities.display(target);
	}
}
