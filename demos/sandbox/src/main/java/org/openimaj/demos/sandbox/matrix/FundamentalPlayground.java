package org.openimaj.demos.sandbox.matrix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.local.list.FileLocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.feature.local.matcher.quantised.BasicQuantisedKeypointMatcher;
import org.openimaj.feature.local.quantised.QuantisedLocalFeature;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.KeypointLocation;
import org.openimaj.image.feature.local.keypoints.quantised.QuantisedKeypoint;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.transforms.FundamentalModel;
import org.openimaj.math.geometry.transforms.HomographyModel;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.util.pair.Pair;

public class FundamentalPlayground {
	public static void main(String[] args) throws IOException {
		String q1 = "00018";
		String q2 = "00019";
		String ukbenchBase = "/Users/jon/Data/ukbench";
		String quantisedBase = ukbenchBase + "/q-sift-1m-rnd/";
		String imageBase = ukbenchBase + "/full/";
		File fq1 = new File(quantisedBase,"ukbench"+q1+".jpg.loc");
		File fq2 = new File(quantisedBase,"ukbench"+q2+".jpg.loc");
		File fim1 = new File(imageBase,"ukbench"+q1+".jpg");
		File fim2 = new File(imageBase,"ukbench"+q2+".jpg");
		List<QuantisedKeypoint> ql1 = FileLocalFeatureList.read(fq1, QuantisedKeypoint.class);
		List<QuantisedKeypoint> ql2 = FileLocalFeatureList.read(fq2, QuantisedKeypoint.class);
		
		MBFImage im1 = ImageUtilities.readMBF(fim1);
		MBFImage im2 = ImageUtilities.readMBF(fim2);
		
//		BasicQuantisedKeypointMatcher<QuantisedKeypoint> matcher = new BasicQuantisedKeypointMatcher<QuantisedKeypoint>(false);
		
		FundamentalModel model = new FundamentalModel(new FundamentalModel.SampsonGeometricErrorCondition(0.75));
		RANSAC<Point2d, Point2d> ransac = new RANSAC<Point2d, Point2d>(model, 1500, new RANSAC.BestFitStoppingCondition(), true);
		ConsistentLocalFeatureMatcher2d<QuantisedKeypoint> matcher = new ConsistentLocalFeatureMatcher2d<QuantisedKeypoint>(new BasicQuantisedKeypointMatcher<QuantisedKeypoint>(false));
		matcher.setFittingModel(ransac);
		matcher.setModelFeatures(ql1);
		boolean foundMatch = matcher.findMatches(ql2);
		System.out.println("Found matches: " + foundMatch);
		System.out.println("Correct vs incorrect: " + (matcher.getMatches().size() / (double)matcher.getAllMatches().size()));
		
		List<Pair<QuantisedKeypoint>> matches = matcher.getMatches();
		
		DisplayUtilities.display(MatchingUtilities.drawMatches(im1, im2, matcher.getAllMatches(), RGBColour.RED));
		DisplayUtilities.display(MatchingUtilities.drawMatches(im1, im2, matches, RGBColour.BLUE));
	}
}
