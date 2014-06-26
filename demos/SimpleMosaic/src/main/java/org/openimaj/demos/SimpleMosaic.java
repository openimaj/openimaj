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
package org.openimaj.demos;

import java.io.File;
import java.io.IOException;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processing.transform.ProjectionProcessor;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.transforms.HomographyModel;
import org.openimaj.math.geometry.transforms.residuals.SingleImageTransferResidual2d;
import org.openimaj.math.model.fit.RANSAC;

/**
 * Demonstration of building an image mosiac
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class SimpleMosaic {
	/**
	 * Build a mosaic
	 * 
	 * @param args
	 *            ignored
	 * @throws IOException
	 */
	public static void main(String args[]) throws IOException {
		final ResizeProcessor rp = new ResizeProcessor(800, 600);
		final DoGSIFTEngine engine = new DoGSIFTEngine();

		final MBFImage imageMiddle = ImageUtilities.readMBF(new File("data/trento-view-1.jpg"));
		imageMiddle.processInplace(rp);
		final FImage workingImageMiddle = Transforms.calculateIntensityNTSC(imageMiddle);
		final LocalFeatureList<Keypoint> middleKP = engine.findFeatures(workingImageMiddle);

		final ConsistentLocalFeatureMatcher2d<Keypoint> matcher =
				new ConsistentLocalFeatureMatcher2d<Keypoint>(new FastBasicKeypointMatcher<Keypoint>(8));
		final HomographyModel model = new HomographyModel();
		final RANSAC<Point2d, Point2d, HomographyModel> modelFitting =
				new RANSAC<Point2d, Point2d, HomographyModel>(model,
						new SingleImageTransferResidual2d<HomographyModel>(), 8.0, 1600,
						new RANSAC.BestFitStoppingCondition(), true);
		matcher.setFittingModel(modelFitting);
		matcher.setModelFeatures(middleKP);
		final ProjectionProcessor<Float[], MBFImage> ptp = new ProjectionProcessor<Float[], MBFImage>();
		imageMiddle.accumulateWith(ptp);

		final MBFImage imageRight = ImageUtilities.readMBF(new File("data/trento-view-0.jpg"));
		imageRight.processInplace(rp);
		final FImage workingImageRight = Transforms.calculateIntensityNTSC(imageRight);
		final LocalFeatureList<Keypoint> rightKP = engine.findFeatures(workingImageRight);
		matcher.findMatches(rightKP);
		ptp.setMatrix(model.getTransform());
		imageRight.accumulateWith(ptp);

		final MBFImage imageLeft = ImageUtilities.readMBF(new File("data/trento-view-2.jpg"));
		imageLeft.processInplace(rp);
		final FImage workingImageLeft = Transforms.calculateIntensityNTSC(imageLeft);
		final LocalFeatureList<Keypoint> leftKP = engine.findFeatures(workingImageLeft);
		matcher.findMatches(leftKP);
		ptp.setMatrix(model.getTransform());
		imageLeft.accumulateWith(ptp);

		final MBFImage projected = ptp.performBlendedProjection(
				(-imageMiddle.getWidth()),
				imageMiddle.getWidth() + imageMiddle.getWidth(),
				-imageMiddle.getHeight() / 2, 3 * imageMiddle.getHeight() / 2, (Float[]) null);
		DisplayUtilities.display(projected);
		ImageUtilities.write(projected, "png", new File("/Users/jsh2/Desktop/mosaic.png"));
	}
}
