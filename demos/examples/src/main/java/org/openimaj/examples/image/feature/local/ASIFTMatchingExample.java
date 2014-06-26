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
package org.openimaj.examples.image.feature.local;

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
import org.openimaj.math.geometry.transforms.HomographyRefinement;
import org.openimaj.math.geometry.transforms.estimation.RobustHomographyEstimator;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.util.pair.Pair;

/**
 * Example showing how to extract ASIFT features and match them.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class ASIFTMatchingExample {
	/**
	 * Main method
	 * 
	 * @param args
	 *            ignored
	 * @throws IOException
	 *             if the image can't be read
	 */
	public static void main(String[] args) throws IOException {
		// Read the images from two streams
		final String input_1Str = "/org/openimaj/examples/image/input_0.png";
		final String input_2Str = "/org/openimaj/examples/image/input_1.png";
		final FImage input_1 = ImageUtilities.readF(ASIFTMatchingExample.class.getResourceAsStream(input_1Str));
		final FImage input_2 = ImageUtilities.readF(ASIFTMatchingExample.class.getResourceAsStream(input_2Str));

		// Prepare the engine to the parameters in the IPOL demo
		final ASIFTEngine engine = new ASIFTEngine(false, 7);

		// Extract the keypoints from both images
		final LocalFeatureList<Keypoint> input1Feats = engine.findKeypoints(input_1);
		System.out.println("Extracted input1: " + input1Feats.size());
		final LocalFeatureList<Keypoint> input2Feats = engine.findKeypoints(input_2);
		System.out.println("Extracted input2: " + input2Feats.size());

		// Prepare the matcher, uncomment this line to use a basic matcher as
		// opposed to one that enforces homographic consistency
		// LocalFeatureMatcher<Keypoint> matcher = createFastBasicMatcher();
		final LocalFeatureMatcher<Keypoint> matcher = createConsistentRANSACHomographyMatcher();

		// Find features in image 1
		matcher.setModelFeatures(input1Feats);
		// ... against image 2
		matcher.findMatches(input2Feats);

		// Get the matches
		final List<Pair<Keypoint>> matches = matcher.getMatches();
		System.out.println("NMatches: " + matches.size());

		// Display the results
		final MBFImage inp1MBF = input_1.toRGB();
		final MBFImage inp2MBF = input_2.toRGB();
		DisplayUtilities.display(MatchingUtilities.drawMatches(inp1MBF, inp2MBF, matches, RGBColour.RED));
	}

	/**
	 * @return a matcher with a homographic constraint
	 */
	private static LocalFeatureMatcher<Keypoint> createConsistentRANSACHomographyMatcher() {
		final ConsistentLocalFeatureMatcher2d<Keypoint> matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
				createFastBasicMatcher());
		matcher.setFittingModel(new RobustHomographyEstimator(10.0, 1000, new RANSAC.BestFitStoppingCondition(),
				HomographyRefinement.NONE));

		return matcher;
	}

	/**
	 * @return a basic matcher
	 */
	private static LocalFeatureMatcher<Keypoint> createFastBasicMatcher() {
		return new FastBasicKeypointMatcher<Keypoint>(8);
	}
}
