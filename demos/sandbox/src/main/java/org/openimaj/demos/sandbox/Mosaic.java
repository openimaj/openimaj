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

import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processing.transform.ProjectionProcessor;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.transforms.HomographyModel;
import org.openimaj.math.geometry.transforms.residuals.SingleImageTransferResidual2d;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.util.pair.Pair;

import Jama.Matrix;

public class Mosaic {
	public static void main(String[] args) throws IOException {
		final ProjectionProcessor<Float[], MBFImage> bpp = new ProjectionProcessor<Float[], MBFImage>();
		final File[] imagesToCombineInOrder = new File[] {
				new File("/Users/jon/Work/openimaj/trunk/demos/SimpleMosaic/data/trento-view-0.jpg"),
				new File("/Users/jon/Work/openimaj/trunk/demos/SimpleMosaic/data/trento-view-1.jpg"),
				new File("/Users/jon/Work/openimaj/trunk/demos/SimpleMosaic/data/trento-view-2.jpg"),
				new File("/Users/jon/Work/openimaj/trunk/demos/SimpleMosaic/data/trento-view-3.jpg"),
		};
		// AffineTransformModel model = new AffineTransformModel(6.0f);
		final HomographyModel model = new HomographyModel();
		final RANSAC<Point2d, Point2d, HomographyModel> ransac = new RANSAC<Point2d, Point2d, HomographyModel>(model,
				new SingleImageTransferResidual2d<HomographyModel>(), 12.0, 600,
				new RANSAC.BestFitStoppingCondition(), true);
		final ConsistentLocalFeatureMatcher2d<Keypoint> matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
				new FastBasicKeypointMatcher<Keypoint>(8));
		matcher.setFittingModel(ransac);
		final int centerImageIndex = 1;
		// double scaleFactor = 1;
		final ResizeProcessor displayResize = new ResizeProcessor(800, 600, false);
		final ResizeProcessor analysisResize = new ResizeProcessor(800, 600);

		final MBFImage centerImage = ImageUtilities.readMBF(imagesToCombineInOrder[centerImageIndex]).process(
				analysisResize);
		final FImage centerImagef = Transforms.calculateIntensityNTSC(centerImage);

		final DoGSIFTEngine engine = new DoGSIFTEngine();
		final List<Keypoint> centerKeys = engine.findFeatures(centerImagef);

		// GO LEFT
		MBFImage currentImage = centerImage;
		FImage currentImagef = centerImagef;
		List<Keypoint> currentKeys = centerKeys;
		bpp.setMatrix(new Matrix(new double[][] { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } }));
		bpp.accumulate(centerImage);
		int steps = 1;
		for (int i = centerImageIndex - 1; i >= 0; i--) {

			final MBFImage nextImage = ImageUtilities.readMBF(imagesToCombineInOrder[i]).process(analysisResize);
			final FImage nextImagef = Transforms.calculateIntensityNTSC(nextImage);

			final List<Keypoint> keys2 = engine.findFeatures(nextImagef);
			matcher.setModelFeatures(currentKeys);
			matcher.findMatches(keys2);

			// FIXME: there should be a class/method for drawing matches
			final MBFImage tmp = new MBFImage(currentImagef.width + nextImagef.width, Math.max(currentImagef.height,
					nextImagef.height), 3);
			final MBFImageRenderer r = tmp.createRenderer();
			r.drawImage(currentImage, 0, 0);
			r.drawImage(nextImage, currentImagef.width, 0);
			for (final Pair<Keypoint> m : matcher.getMatches()) {
				r.drawLine((int) m.secondObject().x, (int) m.secondObject().y,
						(int) m.firstObject().x + currentImagef.width, (int) m.firstObject().y,
						RGBColour.RED);
			}

			DisplayUtilities.display(tmp);
			System.out.println("GOING LEFT");
			final Matrix transform = model.getTransform();
			model.getTransform().print(5, 5);

			bpp.setMatrix(bpp.getMatrix().times(transform));
			final int propWidth = (int) (nextImage.getWidth() * Math.pow(0.5, 0));
			bpp.accumulate(nextImage.extractROI(nextImage.getWidth() - propWidth, 0, propWidth, nextImage.getHeight()));
			currentImage = nextImage;
			currentImagef = nextImagef;
			currentKeys = keys2;
			steps++;

		}

		// GO RIGHT
		currentImage = centerImage;
		currentImagef = centerImagef;
		currentKeys = centerKeys;
		bpp.setMatrix(new Matrix(new double[][] { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } }));
		steps = 1;
		for (int i = centerImageIndex + 1; i < imagesToCombineInOrder.length; i++) {
			final MBFImage nextImage = ImageUtilities.readMBF(imagesToCombineInOrder[i]).process(analysisResize);
			final FImage nextImagef = Transforms.calculateIntensityNTSC(nextImage);

			final List<Keypoint> keys2 = engine.findFeatures(nextImagef);
			matcher.setModelFeatures(currentKeys);
			matcher.findMatches(keys2);

			// FIXME: there should be a class/method for drawing matches
			final MBFImage tmp = new MBFImage(currentImagef.width + nextImagef.width, Math.max(currentImagef.height,
					nextImagef.height), 3);
			final MBFImageRenderer r = tmp.createRenderer();
			r.drawImage(currentImage, 0, 0);
			r.drawImage(nextImage, currentImagef.width, 0);
			for (final Pair<Keypoint> m : matcher.getMatches()) {
				r.drawLine((int) m.secondObject().x, (int) m.secondObject().y,
						(int) m.firstObject().x + currentImagef.width, (int) m.firstObject().y,
						RGBColour.RED);
			}

			DisplayUtilities.display(tmp);

			final Matrix transform = model.getTransform();
			System.out.println("GOING RIGHT");
			transform.print(5, 5);

			bpp.setMatrix(bpp.getMatrix().times(transform));
			final int propWidth = (int) (nextImage.getWidth() * Math.pow(0.5, steps));
			bpp.accumulate(nextImage.extractROI(0, 0, propWidth, nextImage.getHeight()));
			currentImage = nextImage;
			currentImagef = nextImagef;
			currentKeys = keys2;
			steps++;

		}

		DisplayUtilities.display(bpp.performProjection().process(displayResize));

		// MBFImage image1 = ImageUtilities.readMBF(new
		// File("/Users/ss/Desktop/trento-view-0.jpg")).halfImageSize();
		// MBFImage image2 = ImageUtilities.readMBF(new
		// File("/Users/ss/Desktop/trento-view-1.jpg")).halfImageSize();
		//
		// FImage image1f = Transforms.calculateIntensityNTSC(image1);
		// FImage image2f = Transforms.calculateIntensityNTSC(image2);
		//
		// KeypointEngine engine = new KeypointEngine();
		// List<Keypoint> keys1 = engine.findKeypoints(image1f);
		// List<Keypoint> keys2 = engine.findKeypoints(image2f);
		//
		// // HomographyModel model = new HomographyModel(6.0f);
		// // RANSAC<Point2d, Point2d> ransac = new RANSAC<Point2d,
		// Point2d>(model, 100, 10, false);
		// //
		// // ConsistentKeypointMatcher<Keypoint> matcher = new
		// ConsistentKeypointMatcher<Keypoint>(8,0);
		// matcher.setFittingModel(ransac);
		// matcher.setModelKeypoints(keys1);
		// matcher.findMatches(keys2);
		//
		// //FIXME: there should be a class/method for drawing matches
		// MBFImage tmp = new MBFImage(Math.max(image1f.rows, image2f.rows),
		// image1f.cols + image2f.cols, 3);
		// tmp.drawImage(image1, 0, 0);
		// tmp.drawImage(image2, image1f.cols, 0);
		// // It is not drawing any matches??
		// for (Pair<Keypoint> m : matcher.getMatches()) {
		// tmp.drawLine((int)m.secondObject().col, (int)m.secondObject().row,
		// (int)m.firstObject().col + image1f.cols, (int)m.firstObject().row,
		// RGBColour.RED);
		// }
		//
		// DisplayUtilities.display(tmp);
		//
		// Matrix transform = model.getTransform();
		// model.getTransform().print(5,5);
		//
		// BackProjectionProcessor<Float[],MBFImage> bpp = new
		// BackProjectionProcessor<Float[],MBFImage>();
		// bpp.processImage(image1);
		// bpp.setMatrix(transform);
		// bpp.processImage(image2);
		// //FIXME: there should be a class to do back-projection...
		// MBFImage img = new MBFImage(4000,2524,3);
		// for(int x = 0 ; x < img.getWidth(); x ++ ) {
		// for(int y = 0 ; y < img.getHeight(); y ++ ) {
		// int xx = x - 1500;
		// int yy = y - 1500;
		//
		// double xt = transform.get(0, 0) * xx + transform.get(0, 1) * yy +
		// transform.get(0, 2);
		// double yt = transform.get(1, 0) * xx + transform.get(1, 1) * yy +
		// transform.get(1, 2);
		//
		// double zt = transform.get(2, 0) * xx + transform.get(2, 1) * yy +
		// transform.get(2, 2);
		// xt /= zt;
		// yt /= zt;
		//
		// if (xt >=0 && yt>=0 && xt<=image1.getCols() && yt<= image1.getRows())
		// img.setPixel(x, y, image1.getPixelInterp(xt, yt));
		// }
		// }
		// img.drawImage(image2, 1500, 1500);
		// MBFImage img = bpp.performBackProjection();
		// img = img.trim();

		// DisplayUtilities.display(img.process(displayResize));
		// DisplayUtilities.display(img.halfImageSize());
	}
}
