package org.openimaj.demos.sandbox.features;

import java.io.IOException;
import java.util.List;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.asift.ASIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.transforms.HomographyModel;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.util.pair.Pair;

public class ASIFTMatches {
	public static void main(String[] args) throws IOException {
		// Read the images from two streams, (you should change this code to read from two files on your system)
		String input_1Str = "/org/openimaj/demos/sandbox/features/input_0.png";
		String input_2Str = "/org/openimaj/demos/sandbox/features/input_1.png";
		FImage input_1 = ImageUtilities.readF(ASIFTMatches.class.getResourceAsStream(input_1Str));
		FImage input_2 = ImageUtilities.readF(ASIFTMatches.class.getResourceAsStream(input_2Str));
		// Prepare the engine to the parameters in the IPOL demo
		ASIFTEngine engine = new ASIFTEngine(false,7);
		
		// Extract the keypoints from both images
		LocalFeatureList<Keypoint> input1Feats = engine.findKeypoints(input_1);
		System.out.println("Extracted input1: " + input1Feats.size());
		LocalFeatureList<Keypoint> input2Feats = engine.findKeypoints(input_2);
		System.out.println("Extracted input2: " + input2Feats.size());
		
		// Prepare the matcher, uncomment this line to use a basic matcher as opposed to one that enforces homographic consistency
//		LocalFeatureMatcher<Keypoint> matcher = fastBasic();
		LocalFeatureMatcher<Keypoint> matcher = consistentRANSACHomography();
		
		// Find features in image 1
		matcher.setModelFeatures(input1Feats);
		// ... against image 2
		matcher.findMatches(input2Feats);
		
		// Get the matches
		List<Pair<Keypoint>> matches = matcher.getMatches();
		System.out.println("NMatches: " + matches.size());
		// Display the results
		MBFImage inp1MBF = input_1.toRGB();
		MBFImage inp2MBF = input_2.toRGB();
		DisplayUtilities.display(MatchingUtilities.drawMatches(inp1MBF, inp2MBF, matches, RGBColour.RED));
	}

	private static LocalFeatureMatcher<Keypoint> consistentRANSACHomography() {
		HomographyModel model = new HomographyModel(10);
		RANSAC<Point2d, Point2d> ransac = new RANSAC<Point2d, Point2d>(model, 1000, new RANSAC.BestFitStoppingCondition(), true);
		ConsistentLocalFeatureMatcher2d<Keypoint> matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(fastBasic());
		matcher.setFittingModel(ransac);
		return matcher;
	}

	private static LocalFeatureMatcher<Keypoint> fastBasic() {
		return new FastBasicKeypointMatcher<Keypoint>(8);
	}
}
