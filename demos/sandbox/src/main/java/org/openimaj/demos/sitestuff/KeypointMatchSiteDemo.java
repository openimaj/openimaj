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
import org.openimaj.math.geometry.transforms.residuals.SingleImageTransferResidual2d;
import org.openimaj.math.model.fit.RANSAC;

import Jama.Matrix;

public class KeypointMatchSiteDemo {
	static File monaLisaSource = new File("/Users/ss/Desktop/Van-Gogh-Starry-Nights.jpg");
	static File monaLisaTarget = new File(
			"/Users/ss/Desktop/4172349-A_SPECIAL_EXHIBITION_VAN_GOGHS_STARRY_NIGHT_New_Haven.jpg");

	public static void main(String[] args) throws IOException {
		final MBFImage sourceC = ImageUtilities.readMBF(monaLisaSource);
		final MBFImage targetC = ImageUtilities.readMBF(monaLisaTarget);
		final FImage source = sourceC.flatten();
		final FImage target = targetC.flatten();

		final DoGSIFTEngine eng = new DoGSIFTEngine();

		final LocalFeatureList<Keypoint> sourceFeats = eng.findFeatures(source);
		final LocalFeatureList<Keypoint> targetFeats = eng.findFeatures(target);

		final HomographyModel model = new HomographyModel();
		final SingleImageTransferResidual2d<HomographyModel> errorModel = new SingleImageTransferResidual2d<HomographyModel>();
		final RANSAC<Point2d, Point2d, HomographyModel> ransac = new RANSAC<Point2d, Point2d, HomographyModel>(model,
				errorModel, 5f, 1500, new RANSAC.BestFitStoppingCondition(), true);
		final ConsistentLocalFeatureMatcher2d<Keypoint> matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
				new FastBasicKeypointMatcher<Keypoint>(8));
		matcher.setFittingModel(ransac);

		matcher.setModelFeatures(sourceFeats);
		matcher.findMatches(targetFeats);

		final Matrix boundsToPoly = model.getTransform().inverse();

		final Shape s = source.getBounds().transform(boundsToPoly);
		targetC.drawShape(s, 10, RGBColour.BLUE);
		final MBFImage matches = MatchingUtilities.drawMatches(sourceC, targetC, matcher.getMatches(), RGBColour.RED);

		matches.processInplace(new ResizeProcessor(640, 480));
		DisplayUtilities.display(matches);
		ImageUtilities.write(matches, new File("/Users/ss/Desktop/keypoint-match-example.png"));

	}
}
