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
package org.openimaj.demos.image;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.openimaj.demos.Demo;
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
 * Demonstrates using the SIFT keypoint matching and automatic homography
 * transform calculation to project 3 photos into a basic stitched panorama.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @created 15 Feb 2012
 */
@Demo(
		author = "Sina Samangooei",
		description = "Demonstrates using the SIFT keypoint matching and automatic "
				+ "homography transform calculation to project 3 photos into a basic stitched "
				+ "panorama.",
		keywords = { "image", "photo", "mosaic", "stitch", "panorama" },
		title = "Simple Photo Mosaic",
		icon = "/org/openimaj/demos/icons/image/mosaic-icon.png",
		screenshot = "/org/openimaj/demos/screens/image/mosaic-screen.png",
		vmArguments = "-Xmx1G")
public class SimpleMosaic
{
	/**
	 * Default main
	 * 
	 * @param args
	 *            Command-line arguments
	 * @throws IOException
	 */
	public static void main(String args[]) throws IOException
	{
		final JLabel l = new JLabel();
		l.setHorizontalAlignment(JLabel.CENTER);
		final JFrame f = new JFrame("Mosaic Progress");
		f.getContentPane().add(l);
		f.setSize(500, 80);
		f.setVisible(true);

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					// Set up all the various processors
					l.setText("Setting up processors");
					final ResizeProcessor rp = new ResizeProcessor(800, 600);
					final DoGSIFTEngine engine = new DoGSIFTEngine();

					final ConsistentLocalFeatureMatcher2d<Keypoint> matcher =
							new ConsistentLocalFeatureMatcher2d<Keypoint>(
									new FastBasicKeypointMatcher<Keypoint>(8));
					final HomographyModel model = new HomographyModel();
					final RANSAC<Point2d, Point2d, HomographyModel> modelFitting = new RANSAC<Point2d, Point2d, HomographyModel>(
							model, new SingleImageTransferResidual2d<HomographyModel>(), 8.0, 1600,
							new RANSAC.BestFitStoppingCondition(),
							true);
					matcher.setFittingModel(modelFitting);

					// Load in the first (middle) image and calculate the SIFT
					// features
					final MBFImage imageMiddle = ImageUtilities.readMBF(SimpleMosaic.class
							.getResource("/org/openimaj/demos/image/mosaic/trento-view-1.jpg"));
					imageMiddle.processInplace(rp);
					final FImage workingImageMiddle = Transforms.calculateIntensityNTSC(imageMiddle);
					l.setText("Calculating features on middle image");
					final LocalFeatureList<Keypoint> middleKP = engine.findFeatures(workingImageMiddle);
					matcher.setModelFeatures(middleKP);

					// Calculate the projection for the first image
					final ProjectionProcessor<Float[], MBFImage> ptp =
							new ProjectionProcessor<Float[], MBFImage>();
					imageMiddle.accumulateWith(ptp);

					// Load in the right-hand image and calculate its features
					final MBFImage imageRight = ImageUtilities.readMBF(SimpleMosaic.class
							.getResource("/org/openimaj/demos/image/mosaic/trento-view-0.jpg"));
					imageRight.processInplace(rp);
					final FImage workingImageRight = Transforms.calculateIntensityNTSC(imageRight);
					l.setText("Calculating features on right image");
					final LocalFeatureList<Keypoint> rightKP = engine.findFeatures(workingImageRight);

					// Find matches with the middle image
					l.setText("Finding matches with middle image and right image");
					matcher.findMatches(rightKP);
					ptp.setMatrix(model.getTransform());
					l.setText("Projecting right image");
					imageRight.accumulateWith(ptp);

					// Load in the left-hand image and calculate its features
					final MBFImage imageLeft = ImageUtilities.readMBF(SimpleMosaic.class
							.getResource("/org/openimaj/demos/image/mosaic/trento-view-2.jpg"));
					imageLeft.processInplace(rp);
					final FImage workingImageLeft = Transforms.calculateIntensityNTSC(imageLeft);
					l.setText("Calculating features on left image");
					final LocalFeatureList<Keypoint> leftKP = engine.findFeatures(workingImageLeft);

					// Find the matches with the middle image
					l.setText("Finding matches with middle image and left image");
					matcher.findMatches(leftKP);
					ptp.setMatrix(model.getTransform());
					l.setText("Projecting left image");
					imageLeft.accumulateWith(ptp);

					l.setText("Projecting final image");
					final MBFImage projected = ptp.performBlendedProjection(
							(-imageMiddle.getWidth()),
							imageMiddle.getWidth() + imageMiddle.getWidth(),
							-imageMiddle.getHeight() / 2, 3 * imageMiddle.getHeight() / 2,
							(Float[]) null);

					// Display the result
					f.setVisible(false);
					DisplayUtilities.display(projected.process(rp));

					// Write the result
					ImageUtilities.write(projected, "png", new File(
							"/Users/jsh2/Desktop/mosaic.png"));
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}).start();
	}
}
