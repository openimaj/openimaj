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
package org.openimaj.demos.sandbox.siftlike;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.feature.OrientedFeatureVector;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.edges.StrokeWidthTransform;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;
import org.openimaj.util.set.DisjointSetForest;

import Jama.Matrix;

public class SWTSIFT {
	public static void main(String[] args) throws IOException {
		final FImage image = ImageUtilities
				.readF(new File(
						"/Users/jon/Library/Containers/com.apple.mail/Data/Library/Mail Downloads/6244F204-C7AC-441C-853E-51B0E1D722D6/standard-conference-name-badge-holder.png"));

		image.processInplace(new StrokeWidthTransform(true, 1f));

		StrokeWidthTransform.normaliseImage(image);

		DisplayUtilities.display(image);

		// FImage img = ImageUtilities
		// .readF(new File(
		// "/Users/jon/Library/Containers/com.apple.mail/Data/Library/Mail Downloads/4375E35B-F73B-4FF4-B45A-2E2A8D05A62D/example images/typical examples/G0020428.JPG"));
		//
		// // img = ResizeProcessor.halfSize(img);
		// img = img.extractROI(800, 200, 1000, 400);
		//
		// DisplayUtilities.display(img);
		// final StrokeWidthTransform swt = new StrokeWidthTransform(false,
		// 1.f);
		// img.processInplace(swt);
		// DisplayUtilities.display(StrokeWidthTransform.normaliseImage(img));

		// final FImage logo = ImageUtilities.readF(new URL(
		// "http://www.linkshop.com.cn/upload/joinbook/2011/201183951496638.jpg")).inverse();
		//
		// final Map<Ellipse, OrientedFeatureVector> im1f =
		// extractFeatures(img);
		// final Map<Ellipse, OrientedFeatureVector> im2f =
		// extractFeatures(logo);
		//
		// final MBFImage tmp = new MBFImage(img.width + logo.width,
		// Math.max(img.height, logo.height));
		// tmp.drawImage(img.toRGB(), 0, 0);
		// tmp.drawImage(logo.toRGB(), img.width, 0);
		//
		// for (final Entry<Ellipse, OrientedFeatureVector> i1 :
		// im1f.entrySet()) {
		// double minDistance = Double.MAX_VALUE, minDistance2 =
		// Double.MAX_VALUE;
		// Ellipse bestEllipse = null;
		// for (final Entry<Ellipse, OrientedFeatureVector> i2 :
		// im2f.entrySet()) {
		// final double distance = i1.getValue().compare(i2.getValue(),
		// ByteFVComparison.EUCLIDEAN);
		// if (distance < minDistance) {
		// minDistance2 = minDistance;
		// minDistance = distance;
		// bestEllipse = i2.getKey();
		// } else if (distance < minDistance2) {
		// minDistance2 = distance;
		// }
		// }
		//
		// if (minDistance < 0.9 * minDistance2) {
		// tmp.drawShape(i1.getKey(), RGBColour.RED);
		// final Ellipse be = bestEllipse.clone();
		// be.translate(img.width, 0);
		// tmp.drawShape(be, RGBColour.RED);
		//
		// tmp.drawLine(i1.getKey().calculateCentroid(), be.calculateCentroid(),
		// RGBColour.BLUE);
		// }
		// }
		// DisplayUtilities.display(tmp);
	}

	private static Map<Ellipse, OrientedFeatureVector> extractFeatures(FImage img) {
		final StrokeWidthTransform swt = new StrokeWidthTransform(false, 1.0f);
		final FImage swtImage = img.process(swt);
		DisplayUtilities.display(StrokeWidthTransform.normaliseImage(swtImage));

		final EllipseGradientFeatureExtractor egfe = new EllipseGradientFeatureExtractor();

		final List<ConnectedComponent> ccs = findComponents(swtImage);
		final Map<Ellipse, OrientedFeatureVector> im1f = new HashMap<Ellipse, OrientedFeatureVector>();
		for (final ConnectedComponent cc : ccs) {
			final double[] centroid = cc.calculateCentroid();
			final Matrix m = computeCovariance(cc, centroid);

			final Ellipse e = EllipseUtilities.ellipseFromCovariance((float) centroid[0], (float) centroid[1], m, 3f);

			for (final OrientedFeatureVector f : egfe.extract(img, e))
				im1f.put(e, f);
		}

		return im1f;
	}

	private static Matrix computeCovariance(ConnectedComponent cc, double[] centroid) {
		final Matrix m = new Matrix(2, 2);
		final double[][] md = m.getArray();

		for (final Pixel p : cc) {
			md[0][0] += ((p.x - centroid[0]) * (p.x - centroid[0]));
			md[1][1] += ((p.y - centroid[1]) * (p.y - centroid[1]));
			md[0][1] += ((p.x - centroid[0]) * (p.y - centroid[1]));
		}

		final int area = cc.calculateArea();
		md[0][0] /= area;
		md[1][1] /= area;
		md[0][1] /= area;
		md[1][0] = md[0][1];

		return m;
	}

	private final static int[][] connect8 = {
			{ -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 }, { -1, -1 }, { 1, -1 }, { -1, 1 }, { 1, 1 } };

	static List<ConnectedComponent> findComponents(FImage image) {
		final DisjointSetForest<Pixel> forest = new DisjointSetForest<Pixel>();

		Pixel current = new Pixel();
		Pixel next = new Pixel();
		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				final float currentValue = image.pixels[y][x];

				if (currentValue > 0 && currentValue != Float.POSITIVE_INFINITY) {
					current.x = x;
					current.y = y;

					if (forest.makeSet(current) != null)
						current = current.clone();

					for (int i = 0; i < connect8.length; i++) {
						final int xx = x + connect8[i][0];
						final int yy = y + connect8[i][1];

						if (xx >= 0 && xx < image.width - 1 && yy >= 0 && yy < image.height - 1) {
							final float value = image.pixels[yy][xx];

							if (value > 0 && value != Float.POSITIVE_INFINITY) {
								next.x = xx;
								next.y = yy;

								if (forest.makeSet(next) != null)
									next = next.clone();

								// if (Math.max(currentValue, value) /
								// Math.min(currentValue, value) < 3)
								forest.union(current, next);
							}
						}
					}
				}
			}
		}

		final List<ConnectedComponent> components = new ArrayList<ConnectedComponent>();
		for (final Set<Pixel> pixels : forest.getSubsets()) {
			components.add(new ConnectedComponent(pixels));
		}

		return components;
	}
}
