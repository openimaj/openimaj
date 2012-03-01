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
