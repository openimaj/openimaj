package org.openimaj.math.geometry.path;

import java.util.Iterator;

import org.openimaj.math.geometry.GeometricObject2d;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;

/**
 * A {@link Path2d} represents an arbitrary path between 2 points in a 2D space.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public interface Path2d extends GeometricObject2d {
	/**
	 * Get the beginning of the path
	 *
	 * @return the beginning
	 */
	public Point2d begin();

	/**
	 * Get the end of the path
	 *
	 * @return the end
	 */
	public Point2d end();

	/**
	 * Convert the path to a polyline representation
	 *
	 * @return a polyline representation of the path
	 */
	public Polyline asPolyline();

	/**
	 * Convert the path to a iterated polyline representation
	 *
	 * @return an iterated polyline representation of the path
	 */
	public Iterator<Line2d> lineIterator();

	/**
	 * Calculate the length of the path
	 * 
	 * @return the path length
	 */
	public double calculateLength();
}
