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
package org.openimaj.examples.image.analysis;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analysis.algorithm.HoughLines;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.line.Line2d.IntersectionResult;
import org.openimaj.math.geometry.line.Line2d.IntersectionType;
import org.openimaj.math.geometry.point.Point2d;

/**
 * Example showing how to find the corners of a chessboard pattern using a hough
 * line detector.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class HoughLineChessboard {
	/**
	 * Main method
	 * 
	 * @param args
	 *            ignored
	 * @throws IOException
	 *             if image can't be loaded
	 */
	public static void main(String[] args) throws IOException {
		final FImage chessboard = ImageUtilities.readF(new URL("http://www.ugcs.caltech.edu/~rajan/REPORT/camera.jpg"));

		final HoughLines hlines = new HoughLines(1.f);
		chessboard.process(new CannyEdgeDetector()).analyseWith(hlines);

		final List<Line2d> lines = hlines.getBestLines(50);
		final List<Point2d> intersections = new ArrayList<Point2d>();
		for (final Line2d inner : lines) {
			for (final Line2d outer : lines) {
				if (inner == outer)
					continue;

				final IntersectionResult intersect = inner.getIntersection(outer);
				if (intersect.type == IntersectionType.INTERSECTING) {
					intersections.add(intersect.intersectionPoint);
				}
			}
		}

		// draw result
		final MBFImage chessboardC = chessboard.toRGB();
		chessboardC.drawLines(lines, 1, RGBColour.RED);
		chessboardC.drawPoints(intersections, RGBColour.GREEN, 3);

		DisplayUtilities.display(chessboardC);
	}
}
