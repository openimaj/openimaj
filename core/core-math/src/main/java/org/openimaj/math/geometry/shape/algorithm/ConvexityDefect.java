package org.openimaj.math.geometry.shape.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Triangle;

/**
 * Class representing a convexity defect of a polygon, with methods for finding
 * said defects. A convexity defect is a triangle formed between two points on
 * the convex hull of a polygon, and the deepest point on the shape polygon
 * between the points on the hull.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class ConvexityDefect {
	/**
	 * The starting point on the convex hull
	 */
	public Point2d start;

	/**
	 * The ending point on the convex hull
	 */
	public Point2d end;

	/**
	 * The deepest point on the shape polygon
	 */
	public Point2d deepestPoint;

	/**
	 * The depth of the deepest point
	 */
	public float depth;

	private ConvexityDefect() {
	}

	/**
	 * Get the triangle represented by this defect.
	 * 
	 * @return the triangle represented by this defect.
	 */
	public Triangle getTriangle() {
		return new Triangle(start, deepestPoint, end);
	}

	/**
	 * Find the defects of the given polygon. The convex hull of the polygon is
	 * computed internally.
	 * 
	 * @param p
	 *            the polygon
	 * @return a list of all the convexity defexts
	 */
	public static List<ConvexityDefect> findDefects(Polygon p) {
		return findDefects(p, p.calculateConvexHull());
	}

	/**
	 * Find the defects of the given polygon, based on the given convex hull
	 * 
	 * @param p
	 *            the polygon
	 * @param hull
	 *            the convex hull of the polygon
	 * @return a list of all the convexity defexts
	 */
	public static List<ConvexityDefect> findDefects(Polygon p, Polygon hull) {
		// test orientation of hull w.r.t poly
		final int index1 = p.points.indexOf(hull.points.get(0));
		final int index2 = p.points.indexOf(hull.points.get(1));
		final int index3 = p.points.indexOf(hull.points.get(2));

		int sign = 0;
		sign += (index2 > index1) ? 1 : 0;
		sign += (index3 > index2) ? 1 : 0;
		sign += (index1 > index3) ? 1 : 0;

		final boolean reverseOri = (sign == 2) ? false : true;

		final List<ConvexityDefect> defects = new ArrayList<ConvexityDefect>();

		for (int i = 0; i < hull.points.size(); i++) {
			final ConvexityDefect defect = new ConvexityDefect();
			defect.start = hull.get(i);

			if (i == hull.points.size() - 1) {
				defect.end = hull.get(0);
			} else {
				defect.end = hull.get(i + 1);
			}

			final double dx0 = defect.end.getX() - defect.start.getX();
			final double dy0 = defect.end.getY() - defect.start.getY();
			final double scale = 1f / Math.sqrt(dx0 * dx0 + dy0 * dy0);

			float depth = 0;
			boolean isDefect = false;
			int curi = p.points.indexOf(defect.start);
			while (true) {
				if (reverseOri) {
					curi--;
					if (curi < 0)
						curi = p.points.size() - 1;
				} else {
					curi++;
					if (curi >= p.points.size())
						curi = 0;
				}

				final Point2d cur = p.points.get(curi);
				if (cur == defect.end)
					break;

				final double dx = (double) cur.getX() - (double) defect.start.getX();
				final double dy = (double) cur.getY() - (double) defect.start.getY();

				/* compute depth */
				final double dist = Math.abs(-dy0 * dx + dx0 * dy) * scale;

				if (dist > depth)
				{
					depth = (float) dist;
					defect.deepestPoint = cur;
					defect.depth = depth;
					isDefect = true;
				}
			}

			if (isDefect) {
				defects.add(defect);
			}
		}

		return defects;
	}
}
