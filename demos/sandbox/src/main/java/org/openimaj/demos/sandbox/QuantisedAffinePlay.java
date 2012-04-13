package org.openimaj.demos.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.feature.local.list.FileLocalFeatureList;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.quantised.BasicQuantisedKeypointMatcher;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.keypoints.quantised.QuantisedKeypoint;
import org.openimaj.util.pair.Pair;

public class QuantisedAffinePlay {
	static String imageRoot = "/Users/ss/Development/data/ukbench/smvs/images";
	static String quantisedRoot = "/Users/ss/Development/data/ukbench/smvs/quantised";
	
	public static void main(String[] args) throws IOException {
		String query = "/query/landmarks/Query/001.jpg";
		String database = "/database/landmarks/Reference/001.jpg";
		
		FileLocalFeatureList<QuantisedKeypoint> queryKP = FileLocalFeatureList.read(new File(quantisedRoot,query),QuantisedKeypoint.class);
		FileLocalFeatureList<QuantisedKeypoint> databaseKP = FileLocalFeatureList.read(new File(quantisedRoot,database),QuantisedKeypoint.class);
		
		MBFImage queryImg = ImageUtilities.readMBF(new File(imageRoot,query));
		MBFImage databaseImg = ImageUtilities.readMBF(new File(imageRoot,database));
		
		BasicQuantisedKeypointMatcher<QuantisedKeypoint> matcher = new BasicQuantisedKeypointMatcher<QuantisedKeypoint>(false);
		
		matcher.setModelFeatures(queryKP);
		matcher.findMatches(databaseKP);
		
		List<Pair<QuantisedKeypoint>> matches = matcher.getMatches();
		
		MBFImage matchesImg = MatchingUtilities.drawMatches(queryImg, databaseImg, matches, RGBColour.RED);
		
		DisplayUtilities.display(matchesImg);
	}
}
