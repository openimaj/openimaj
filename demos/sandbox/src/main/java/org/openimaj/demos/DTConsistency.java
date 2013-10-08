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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.shape.Triangle;
import org.openimaj.math.geometry.triangulation.DelaunayTriangulator;
import org.openimaj.util.pair.Pair;

public class DTConsistency {
	public static int computeDTScore(List<? extends Pair<? extends Point2d>> matches) {
		final int size = matches.size();

		if (size <= 1)
			return 0;

		if (size == 2)
			return 1;

		if (size == 3)
			return 3;

		final List<Triangle> t1 = triangulateFirst(matches);
		final List<Triangle> t2 = triangulateSecond(matches);

		int commonEdges = 0;
		for (int i = 0; i < size; i++) {
			for (int j = i + 1; j < size; j++) {
				final boolean e1 = hasEdge(matches.get(i).firstObject(), matches.get(j).firstObject(), t1);
				final boolean e2 = hasEdge(matches.get(i).secondObject(), matches.get(j).secondObject(), t2);

				if (e1 && e2)
					commonEdges++;
			}
		}

		return commonEdges;
	}

	public static class DTConsistencyInfo {
		public List<Triangle> firstTrianglulation;
		public List<Triangle> secondTrianglulation;
		public List<Line2d> firstCommonEdges = new ArrayList<Line2d>();
		public List<Line2d> secondCommonEdges = new ArrayList<Line2d>();
	}

	public static DTConsistencyInfo computeTriangulationInfo(List<? extends Pair<? extends Point2d>> matches) {
		final int size = matches.size();

		if (size <= 3)
			return null;

		final DTConsistencyInfo info = new DTConsistencyInfo();

		info.firstTrianglulation = triangulateFirst(matches);
		info.secondTrianglulation = triangulateSecond(matches);

		for (int i = 0; i < size; i++) {
			final Point2d firsti = matches.get(i).firstObject();
			final Point2d secondi = matches.get(i).secondObject();

			for (int j = i + 1; j < size; j++) {
				final Point2d firstj = matches.get(j).firstObject();
				final Point2d secondj = matches.get(j).secondObject();

				final boolean e1 = hasEdge(firsti, firstj, info.firstTrianglulation);
				final boolean e2 = hasEdge(secondi, secondj, info.secondTrianglulation);

				if (e1 && e2) {
					info.firstCommonEdges.add(new Line2d(firsti, firstj));
					info.secondCommonEdges.add(new Line2d(secondi, secondj));
				}
			}
		}

		return info;
	}

	@SuppressWarnings("unchecked")
	public static List<Triangle> triangulateFirst(List<? extends Pair<? extends Point2d>> matches) {
		return DelaunayTriangulator.triangulate(Pair.getFirst((Iterable<Pair<Point2d>>) matches));
	}

	@SuppressWarnings("unchecked")
	public static List<Triangle> triangulateSecond(List<? extends Pair<? extends Point2d>> matches) {
		return DelaunayTriangulator.triangulate(Pair.getSecond((Iterable<Pair<Point2d>>) matches));
	}

	/**
	 * Search for an edge between the given points in any of the triangles
	 * 
	 * @param pt1
	 * @param pt2
	 * @param tris
	 * @return
	 */
	private static boolean hasEdge(Point2d pt1, Point2d pt2, List<Triangle> tris) {
		for (final Triangle t : tris) {
			boolean foundP1 = false;
			boolean foundP2 = false;

			for (int i = 0; i < 3; i++) {
				if (t.vertices[i] == pt1)
					foundP1 = true;

				if (t.vertices[i] == pt2)
					foundP2 = true;
			}

			if (foundP1 && foundP2)
				return true;
		}

		return false;
	}

	public static List<Pair<Keypoint>> filterDuplicatePoints(List<Pair<Keypoint>> matches) {
		final List<Point2d> d1 = getDuplicatePoints(Pair.getFirst(matches));
		final List<Point2d> d2 = getDuplicatePoints(Pair.getSecond(matches));

		final List<Pair<Keypoint>> toRemove = new ArrayList<Pair<Keypoint>>();
		for (final Pair<Keypoint> p : matches) {
			for (final Point2d d : d1) {
				if (p.firstObject() == d)
					toRemove.add(p);
			}
		}

		for (final Pair<Keypoint> p : matches) {
			for (final Point2d d : d2) {
				if (p.secondObject() == d)
					toRemove.add(p);
			}
		}

		matches.removeAll(toRemove);

		return matches;
	}

	public static List<Point2d> getDuplicatePoints(List<? extends Point2d> pts) {
		final List<Point2d> dups = new ArrayList<Point2d>();

		for (int i = 0; i < pts.size(); i++) {
			final Point2d pti = pts.get(i);
			for (int j = 0; j < pts.size(); j++) {
				final Point2d ptj = pts.get(j);

				if (i != j) {
					if (pti.getX() == ptj.getX() && pti.getY() == ptj.getY()) {
						dups.add(pti);
						dups.add(ptj);
					}
				}
			}
		}

		return dups;
	}

	public static void main(String[] args) throws IOException {
		final FImage image1 = ImageUtilities.readF(new URL(
				"http://punch-records.co.uk/files/2013/01/Coca-cola-logo-eps-vector-nocturnar-com.jpg"));
		final FImage image2 = ImageUtilities
				.readF(new URL(
						"http://i133.photobucket.com/albums/q78/KylePix/Car%20Shows%20and%20Races/Los%20Angeles%2011/111124-4937CocaColaMotorcycle.jpg"));

		final DoGSIFTEngine engine = new DoGSIFTEngine();
		engine.getOptions().setDoubleInitialImage(true);

		final LocalFeatureList<Keypoint> keys1 = engine.findFeatures(image1);
		final LocalFeatureList<Keypoint> keys2 = engine.findFeatures(image2);

		final FastBasicKeypointMatcher<Keypoint> matcher = new FastBasicKeypointMatcher<Keypoint>(6);
		matcher.setModelFeatures(keys1);
		matcher.findMatches(keys2);

		final List<Pair<Keypoint>> matches = filterDuplicatePoints(matcher.getMatches());

		DisplayUtilities.display(MatchingUtilities.drawMatches(image1, image2, matches, 0F));

		final DTConsistencyInfo info = DTConsistency.computeTriangulationInfo(matches);

		final MBFImage i1 = MBFImage.createRGB(image2);
		final MBFImage i2 = MBFImage.createRGB(image1);

		i1.drawLines(info.firstCommonEdges, 5, RGBColour.BLUE);
		i2.drawLines(info.secondCommonEdges, 5, RGBColour.BLUE);

		for (final Shape s : info.firstTrianglulation)
			i1.drawShape(s, RGBColour.RED);

		for (final Shape s : info.secondTrianglulation)
			i2.drawShape(s, RGBColour.RED);

		DisplayUtilities.display(i1);
		DisplayUtilities.display(i2);
	}
}
