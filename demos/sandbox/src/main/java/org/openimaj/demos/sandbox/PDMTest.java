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
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.point.PointList;
import org.openimaj.math.geometry.point.PointListConnections;

public class PDMTest {
	static List<PointList> loadData() {
		final float[][] rawData = new float[][] {
				{ 576, 303, 579, 418, 570, 466, 457, 415, 408, 355, 495, 216, 420, 99, 696, 81, 677, 206, 792, 382, 772,
					438 },
					{ 442, 289, 444, 392, 442, 433, 417, 428, 326, 401, 393, 215, 268, 169, 514, 156, 550, 218, 626, 412,
						527, 437 },
						{ 548, 317, 543, 423, 543, 464, 512, 463, 403, 434, 493, 254, 388, 216, 581, 187, 643, 243, 746, 447,
							627, 472 },
							{ 563, 302, 561, 404, 537, 450, 536, 464, 425, 421, 476, 231, 349, 191, 616, 160, 656, 223, 753, 435,
								631, 462 },
								{ 550, 267, 555, 375, 532, 413, 488, 442, 407, 392, 470, 193, 349, 164, 617, 124, 654, 189, 742, 405,
									650, 449 },
									{ 565, 318, 578, 441, 566, 477, 513, 489, 409, 445, 490, 231, 356, 112, 739, 88, 673, 222, 787, 461, 670,
										498 },
				// { 557, 312, 572, 434, 548, 472, 556, 464, 512, 481, 395, 438,
				// 477, 229, 352, 118, 715, 88, 671, 219, 783,
				// 455 },
										{ 574, 308, 560, 423, 566, 482, 526, 470, 405, 435, 496, 234, 374, 194, 636, 170, 682, 229, 800, 444,
											665, 476 },
											{ 580, 308, 578, 424, 571, 468, 524, 513, 437, 453, 509, 228, 372, 173, 643, 154, 693, 227, 758, 469,
												668, 512 },
												{ 582, 305, 583, 419, 583, 467, 422, 505, 411, 438, 496, 227, 349, 111, 766, 86, 698, 212, 775, 458, 762,
													527 }
		};

		final List<PointList> ptsL = new ArrayList<PointList>();
		for (int i = 0; i < rawData.length; i++) {
			final PointList pl = new PointList();

			for (int j = 0; j < rawData[i].length; j += 2) {
				final float x = rawData[i][j + 1];
				final float y = rawData[i][j + 0];

				pl.points.add(new Point2dImpl(x, y));
			}

			ptsL.add(pl);
		}

		return ptsL;
	}

	static List<FImage> loadImages() throws IOException {
		final ArrayList<FImage> list = new ArrayList<FImage>();

		for (int i = 1; i <= 10; i++) {
			if (i == 7)
				continue;
			FImage img = ImageUtilities.readF(new File(
					"/Users/jon/Work/students/merlin/individual_project/Source/Data/Pictures/bp" + i + ".JPG"));

			img = img.processInplace(new ResizeProcessor(600, 900, false));

			list.add(img);
		}
		return list;
	}

	static PointListConnections loadConnections() {
		final PointListConnections plc = new PointListConnections();
		plc.addConnection(0, 1);
		plc.addConnection(1, 2);
		plc.addConnection(1, 4);
		plc.addConnection(1, 9);
		plc.addConnection(4, 3);
		plc.addConnection(9, 10);
		plc.addConnection(0, 5);
		plc.addConnection(0, 8);
		plc.addConnection(5, 6);
		plc.addConnection(8, 7);
		return plc;
	}

	// public static void main(String[] args) {
	// final int width = 900, height = 600;
	//
	// final List<PointList> pointData = loadData();
	// final PointListConnections plc = loadConnections();
	//
	// final Float[][] cols = new Float[pointData.get(0).size()][];
	// for (int i = 0; i < cols.length; i++)
	// cols[i] = RGBColour.randomColour();
	//
	// // for (final PointList pl : pointData) {
	// // final MBFImage img = new MBFImage(width, height, 3);
	// // final List<Line2d> lines = plc.getLines(pl);
	// // img.drawLines(lines, 1, RGBColour.RED);
	// //
	// // for (int i = 0; i < pl.size(); i++) {
	// // final Point2d pt = pl.get(i);
	// // img.drawPoint(pt, cols[i], 3);
	// // }
	// // DisplayUtilities.display(img);
	// // }
	//
	// final PointDistributionModel pdm = new PointDistributionModel(pointData);
	// pdm.setNumComponents(2);
	//
	// final double sd = pdm.getStandardDeviations(3.0)[0];
	// VideoDisplay.createVideoDisplay(new AnimatedVideo<MBFImage>(new
	// MBFImage(width, height, 3))
	// {
	// ValueAnimator<Double> a = ForwardBackwardLoopingValueAnimator
	// .loop(new LinearDoubleValueAnimator(-sd, sd, 60));
	//
	// @Override
	// protected void updateNextFrame(MBFImage frame) {
	// frame.fill(RGBColour.BLACK);
	// final Double val = a.nextValue();
	//
	// System.out.println(val);
	// final PointList newShape = pdm.generateNewShape(new double[] {
	// val });
	//
	// final PointList tpts =
	// newShape.transform(TransformUtilities.translateMatrix(width / 2,
	// height / 2).times(
	// TransformUtilities.scaleMatrix(100, 100)));
	//
	// for (int i = 0; i < tpts.size(); i++) {
	// final Point2d pt = tpts.get(i);
	// frame.drawPoint(pt, cols[i], 10);
	// }
	//
	// final List<Line2d> lines = plc.getLines(tpts);
	// frame.drawLines(lines, 5, RGBColour.RED);
	// }
	// });
	// }

	public static void main(String[] args) throws IOException {
		final List<PointList> pointData = loadData();
		final PointListConnections plc = loadConnections();
		final List<FImage> images = loadImages();

		System.out.println(pointData.size());
		System.out.println(images.size());

		final Float[][] cols = new Float[pointData.get(0).size()][];
		for (int i = 0; i < cols.length; i++)
			cols[i] = RGBColour.randomColour();

		for (int j = 0; j < pointData.size(); j++) {
			final PointList pl = pointData.get(j);
			final MBFImage img = images.get(j).toRGB();

			final List<Line2d> lines = plc.getLines(pl);
			img.drawLines(lines, 1, RGBColour.RED);

			for (int i = 0; i < pl.size(); i++) {
				final Point2d pt = pl.get(i);
				img.drawPoint(pt, cols[i], 3);
			}
			DisplayUtilities.display(img);
		}
	}
}
