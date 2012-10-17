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
