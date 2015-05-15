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
package org.openimaj.math.geometry.shape.util;

import odk.lang.FastMath;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.RotatedRectangle;

/**
 * Rotating calipers algorithm, based on the implementation by <a
 * href="https://github.com/bkiers/RotatingCalipers">Bart Kiers</a>.
 * <p>
 * Modified to only use radians for angles and fit better within the OpenIMAJ
 * geometry classes.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Bart Kiers
 *
 */
public final class RotatingCalipers {
	private static final double ANGLE_0DEG_IN_RADS = 0;
	private static final double ANGLE_90DEG_IN_RADS = Math.PI / 2;
	private static final double ANGLE_180DEG_IN_RADS = Math.PI;
	private static final double ANGLE_270DEG_IN_RADS = 3 * Math.PI / 2;
	private static final double ANGLE_360DEG_IN_RADS = 2 * Math.PI;

	protected enum Corner {
		UPPER_RIGHT, UPPER_LEFT, LOWER_LEFT, LOWER_RIGHT
	}

	private static double getArea(Point2dImpl[] rectangle) {

		final double deltaXAB = rectangle[0].x - rectangle[1].x;
		final double deltaYAB = rectangle[0].y - rectangle[1].y;

		final double deltaXBC = rectangle[1].x - rectangle[2].x;
		final double deltaYBC = rectangle[1].y - rectangle[2].y;

		final double lengthAB = FastMath.sqrt((deltaXAB * deltaXAB) + (deltaYAB * deltaYAB));
		final double lengthBC = FastMath.sqrt((deltaXBC * deltaXBC) + (deltaYBC * deltaYBC));

		return lengthAB * lengthBC;
	}

	/**
	 * Use the rotating calipers algorithm to optimally find the minimum sized
	 * rotated rectangle that encompasses the outer shell of the given polygon.
	 *
	 * @param poly
	 *            the polygon
	 * @param assumeSimple
	 *            can the algorithm assume the polygon is simple and use an
	 *            optimised (Melkman's) convex hull?
	 * @return the minimum enclosing rectangle
	 */
	public static RotatedRectangle getMinimumBoundingRectangle(Polygon poly, boolean assumeSimple) {
		final Polygon convexHull = assumeSimple ? poly.calculateConvexHullMelkman() : poly.calculateConvexHull();

		if (convexHull.size() < 3) {
			// FIXME
			return new RotatedRectangle(convexHull.calculateRegularBoundingBox(), 0);
		}

		Point2dImpl[] minimum = null;
		double minimumAngle = 0;
		double area = Double.MAX_VALUE;

		final Caliper I = new Caliper(convexHull, getIndex(convexHull, Corner.UPPER_RIGHT), ANGLE_90DEG_IN_RADS);
		final Caliper J = new Caliper(convexHull, getIndex(convexHull, Corner.UPPER_LEFT), ANGLE_180DEG_IN_RADS);
		final Caliper K = new Caliper(convexHull, getIndex(convexHull, Corner.LOWER_LEFT), ANGLE_270DEG_IN_RADS);
		final Caliper L = new Caliper(convexHull, getIndex(convexHull, Corner.LOWER_RIGHT), ANGLE_0DEG_IN_RADS);

		while (L.currentAngle < ANGLE_90DEG_IN_RADS) {
			final Point2dImpl[] rectangle = new Point2dImpl[] {
					L.getIntersection(I),
					I.getIntersection(J),
					J.getIntersection(K),
					K.getIntersection(L)
			};

			final double tempArea = getArea(rectangle);
			if (minimum == null || tempArea < area) {
				minimum = rectangle;
				minimumAngle = L.currentAngle;
				area = tempArea;
			}

			final double smallestTheta = getSmallestTheta(I, J, K, L);
			I.rotateBy(smallestTheta);
			J.rotateBy(smallestTheta);
			K.rotateBy(smallestTheta);
			L.rotateBy(smallestTheta);
		}

		return makeRotated(minimum, minimumAngle);
	}

	private static RotatedRectangle makeRotated(Point2dImpl[] rectangle, double angle) {
		final double deltaXAB = rectangle[0].x - rectangle[1].x;
		final double deltaYAB = rectangle[0].y - rectangle[1].y;

		final double deltaXBC = rectangle[1].x - rectangle[2].x;
		final double deltaYBC = rectangle[1].y - rectangle[2].y;

		final double lengthAB = FastMath.sqrt((deltaXAB * deltaXAB) + (deltaYAB * deltaYAB));
		final double lengthBC = FastMath.sqrt((deltaXBC * deltaXBC) + (deltaYBC * deltaYBC));

		final double cx = (rectangle[0].x + rectangle[1].x + rectangle[2].x + rectangle[3].x) / 4;
		final double cy = (rectangle[0].y + rectangle[1].y + rectangle[2].y + rectangle[3].y) / 4;

		return new RotatedRectangle(cx, cy, lengthBC, lengthAB, angle);
	}

	private static double getSmallestTheta(Caliper I, Caliper J, Caliper K, Caliper L) {

		final double thetaI = I.getDeltaAngleNextPoint();
		final double thetaJ = J.getDeltaAngleNextPoint();
		final double thetaK = K.getDeltaAngleNextPoint();
		final double thetaL = L.getDeltaAngleNextPoint();

		if (thetaI <= thetaJ && thetaI <= thetaK && thetaI <= thetaL) {
			return thetaI;
		}
		else if (thetaJ <= thetaK && thetaJ <= thetaL) {
			return thetaJ;
		}
		else if (thetaK <= thetaL) {
			return thetaK;
		}
		else {
			return thetaL;
		}
	}

	protected static int getIndex(Polygon convexHull, Corner corner) {

		int index = 0;
		final Point2d point = convexHull.points.get(index);
		float px = point.getX();
		float py = point.getY();

		for (int i = 1; i < convexHull.size() - 1; i++) {

			final Point2d temp = convexHull.points.get(i);
			boolean change = false;

			final float tx = temp.getX();
			final float ty = temp.getY();

			switch (corner) {
			case UPPER_RIGHT:
				change = (tx > px || (tx == px && ty > py));
				break;
			case UPPER_LEFT:
				change = (ty > py || (ty == py && tx < px));
				break;
			case LOWER_LEFT:
				change = (tx < px || (tx == px && ty < py));
				break;
			case LOWER_RIGHT:
				change = (ty < py || (ty == py && tx > px));
				break;
			}

			if (change) {
				index = i;
				px = tx;
				py = ty;
			}
		}

		return index;
	}

	protected static class Caliper {

		final static double SIGMA = 0.00000000001;

		final Polygon convexHull;
		int pointIndex;
		double currentAngle;

		Caliper(Polygon convexHull, int pointIndex, double currentAngle) {
			this.convexHull = convexHull;
			this.pointIndex = pointIndex;
			this.currentAngle = currentAngle;
		}

		double getAngleNextPoint() {
			final Point2d p1 = convexHull.get(pointIndex);
			final Point2d p2 = convexHull.get((pointIndex + 1) % convexHull.size());

			final double deltaX = p2.getX() - p1.getX();
			final double deltaY = p2.getY() - p1.getY();

			final double angle = FastMath.atan2(deltaY, deltaX);

			return angle < 0 ? ANGLE_360DEG_IN_RADS + angle : angle;
		}

		double getConstant() {

			final Point2d p = convexHull.get(pointIndex);

			return p.getY() - (getSlope() * p.getX());
		}

		double getDeltaAngleNextPoint() {

			double angle = getAngleNextPoint();

			angle = angle < 0 ? ANGLE_360DEG_IN_RADS + angle - currentAngle : angle - currentAngle;

			return angle < 0 ? ANGLE_360DEG_IN_RADS : angle;
		}

		Point2dImpl getIntersection(Caliper that) {

			// the x-intercept of 'this' and 'that': x = ((c2 - c1) / (m1 - m2))
			double x;
			// the y-intercept of 'this' and 'that', given 'x': (m*x) + c
			double y;

			if (this.isVertical()) {
				x = convexHull.points.get(pointIndex).getX();
			}
			else if (this.isHorizontal()) {
				x = that.convexHull.points.get(that.pointIndex).getX();
			}
			else {
				x = (that.getConstant() - this.getConstant()) / (this.getSlope() - that.getSlope());
			}

			if (this.isVertical()) {
				y = that.getConstant();
			}
			else if (this.isHorizontal()) {
				y = this.getConstant();
			}
			else {
				y = (this.getSlope() * x) + this.getConstant();
			}

			return new Point2dImpl(x, y);
		}

		double getSlope() {
			return Math.tan(currentAngle);
		}

		boolean isHorizontal() {
			return (Math.abs(currentAngle) < SIGMA) || (Math.abs(currentAngle - ANGLE_180DEG_IN_RADS) < SIGMA);
		}

		boolean isVertical() {
			return (Math.abs(currentAngle - ANGLE_90DEG_IN_RADS) < SIGMA)
					|| (Math.abs(currentAngle - ANGLE_270DEG_IN_RADS) < SIGMA);
		}

		void rotateBy(double angle) {

			if (this.getDeltaAngleNextPoint() == angle) {
				pointIndex++;
			}

			this.currentAngle += angle;
		}
	}
}
