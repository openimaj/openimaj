package org.openimaj.math.geometry.shape;

import org.openimaj.math.geometry.point.Point2d;

import Jama.Matrix;

/**
 * A generalised geometric object that has a calculable
 * centre of gravity and regular bounding box. The object 
 * can also be transformed in a variety of ways. 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public interface GeometricObject {	
	/**
	 * Compute the regular (oriented to the axes) bounding box
	 * of the shape.
	 * 
	 * @return the regular bounding box as [x,y,width,height]
	 */
	public Rectangle calculateRegularBoundingBox();
	
	/**
	 * Translate the shapes position
	 *  
	 * @param x x-translation
	 * @param y y-translation
	 */
	public void translate(float x, float y);
	
	/**
	 * Scale the shape by the given amount about (0,0). Scalefactors
	 * between 0 and 1 shrink the shape. 
	 * @param sc the scale factor.
	 */
	public void scale(float sc);
	
	/**
	 * Scale the shape by the given amount about the given point. 
	 * Scalefactors between 0 and 1 shrink the shape.
	 * @param centre the centre of the scaling operation
	 * @param sc the scale factor
	 */
	public void scale(Point2d centre, float sc);
	
	/**
	 * Scale the shape about its centre of gravity.
	 * Scalefactors between 0 and 1 shrink the shape.
	 * @param sc the scale factor
	 */
	public void scaleCOG( float sc );
	
	/**
	 * Get the centre of gravity of the shape
	 * @return the centre of gravity of the shape
	 */
	public Point2d getCOG();
		
	/**
	 * @return the minimum x-ordinate
	 */
	public double minX();
	
	/**
	 * @return the minimum y-ordinate
	 */
	public double minY();
	
	/**
	 * @return the maximum x-ordinate
	 */
	public double maxX();
	
	/**
	 * @return the maximum y-ordinate
	 */
	public double maxY();
	
	/**
	 * @return the width of the regular bounding box 
	 */
	public double getWidth();
	
	/**
	 * @return the height of the regular bounding box
	 */
	public double getHeight();
	
	/**
	 * Apply a 3x3 transform matrix to a copy of the {@link GeometricObject}
	 * and return it
	 * @param transform 3x3 transform matrix
	 * @return the transformed shape
	 */
	public GeometricObject transform(Matrix transform);
}
