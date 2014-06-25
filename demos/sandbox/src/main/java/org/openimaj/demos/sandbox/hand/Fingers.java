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
package org.openimaj.demos.sandbox.hand;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.connectedcomponent.ConnectedComponentLabeler;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.ConnectedComponent.ConnectMode;
import org.openimaj.image.processing.threshold.OtsuThreshold;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.algorithm.ConvexityDefect;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

public class Fingers {
	public static void main(String[] args) throws VideoCaptureException {
		final VideoCapture vc = new VideoCapture(320, 240);

		final JFrame frame = DisplayUtilities.displaySimple(vc.getNextFrame(), "capture");
		final ConnectedComponentLabeler ccl = new ConnectedComponentLabeler(ConnectMode.CONNECT_4);

		while (true) {
			final MBFImage cimg = vc.getNextFrame();
			final FImage gimg = cimg.flatten();
			gimg.processInplace(new OtsuThreshold());
			// gimg.threshold(0.4f);

			ccl.analyseImage(gimg);
			final ConnectedComponent hand = findBiggest(ccl.getComponents());

			cimg.drawPoints(hand, RGBColour.WHITE, 1);

			if (hand != null) {
				Polygon poly = hand.toPolygon();
				poly = poly.reduceVertices(3);

				final Polygon chull = poly.calculateConvexHull();

				final List<ConvexityDefect> defects = ConvexityDefect.findDefects(poly, chull);
				// for (final ConvexityDefect cd : defects) {
				// cimg.drawShapeFilled(cd.getTriangle(), RGBColour.MAGENTA);
				// }

				final List<Point2d> tips = findTips(defects);

				final Point2d centroid = poly.calculateCentroid();
				System.out.println(centroid);

				for (final Point2d pt : tips) {
					cimg.drawLine(centroid, pt, RGBColour.RED);
					cimg.drawShape(new Circle(pt, 5), RGBColour.CYAN);
				}

				cimg.drawPolygon(poly, 1, RGBColour.RED);
				cimg.drawPolygon(chull, 1, RGBColour.BLUE);
			}

			DisplayUtilities.display(cimg, frame);
		}
	}

	private static final int MIN_FINGER_DEPTH = 20;
	private static final int MAX_FINGER_ANGLE = 60; // degrees

	private static List<Point2d> findTips(List<ConvexityDefect> defects) {
		final ArrayList<Point2d> fingerTips = new ArrayList<Point2d>();

		for (int i = 0; i < defects.size(); i++) {
			if (defects.get(i).depth < MIN_FINGER_DEPTH) // defect too shallow
				continue;

			// look at fold points on either side of a tip
			final int prevIdx = (i == 0) ? (defects.size() - 1) : (i - 1);
			final int nextIdx = (i == defects.size() - 1) ? 0 : (i + 1);

			final int angle = angleBetween(defects.get(i).start, defects.get(prevIdx).deepestPoint,
					defects.get(nextIdx).deepestPoint);
			if (angle >= MAX_FINGER_ANGLE)
				continue; // angle between finger and folds too wide

			// this point is probably a fingertip, so add to list
			fingerTips.add(defects.get(i).start);
		}

		return fingerTips;
	}

	// calculate the angle between the tip and its neighboring folds
	// (in integer degrees)
	private static int angleBetween(Point2d tip, Point2d next, Point2d prev)
	{
		return Math.abs((int) Math.round(
				Math.toDegrees(
						Math.atan2(next.getX() - tip.getX(), next.getY() - tip.getY()) -
								Math.atan2(prev.getX() - tip.getX(), prev.getY() - tip.getY()))));
	}

	static ConnectedComponent findBiggest(List<ConnectedComponent> components) {
		ConnectedComponent biggest = null;
		int size = 0;

		for (final ConnectedComponent cc : components) {
			if (cc.pixels.size() > size) {
				size = cc.pixels.size();
				biggest = cc;
			}
		}

		return biggest;
	}
}
