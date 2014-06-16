/*
 * Copyright (c) 2010, Bart Kiers
 *
 *import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.RotatedRectangle;
ng without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package org.openimaj.math.geometry.shape.util;

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

		final double lengthAB = Math.sqrt((deltaXAB * deltaXAB) + (deltaYAB * deltaYAB));
		final double lengthBC = Math.sqrt((deltaXBC * deltaXBC) + (deltaYBC * deltaYBC));

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

		final double lengthAB = Math.sqrt((deltaXAB * deltaXAB) + (deltaYAB * deltaYAB));
		final double lengthBC = Math.sqrt((deltaXBC * deltaXBC) + (deltaYBC * deltaYBC));

		final double cx = (rectangle[0].x + rectangle[1].x + rectangle[2].x + rectangle[3].x) / 4;
		final double cy = (rectangle[0].y + rectangle[1].y + rectangle[2].y + rectangle[3].y) / 4;

		return new RotatedRectangle(cx, cy, lengthAB, lengthBC, angle);
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

			final double angle = Math.atan2(deltaY, deltaX);

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
