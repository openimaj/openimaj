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
package org.openimaj.math.geometry.shape;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

import Jama.Matrix;

/**
 * A circle shape
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class Circle implements Shape {
	protected Point2d centre;
	protected float radius;
	
	/**
	 * Construct a circle with the given position and radius
	 * @param x the x-coordinate of the centre
	 * @param y the y-coordinate of the centre
	 * @param radius the radius
	 */
	public Circle(float x, float y, float radius) {
		this(new Point2dImpl(x, y), radius);
	}
	
	/**
	 * Construct a circle with the given position and radius
	 * @param centre the coordinate of the centre
	 * @param radius the radius
	 */
	public Circle(Point2d centre, float radius) {
		this.centre = centre;
		this.radius = radius;
	}

	/**
	 * Construct a circle with the given circle
	 * @param c the circle
	 */
	public Circle(Circle c) {
		this.centre = c.centre;
		this.radius = c.radius;
	}

	@Override
	public boolean isInside(Point2d point) {
		double dx = (centre.getX() - point.getX());
		double dy = (centre.getY() - point.getY());
		
		double dist = Math.sqrt(dx*dx + dy*dy);
	    return dist <= radius;
	}

	@Override
	public Rectangle calculateRegularBoundingBox() {
		int x = Math.round(centre.getX() - radius);
		int y = Math.round(centre.getY() - radius);
		int r = Math.round(radius * 2); 
		
		return new Rectangle(x, y, r, r);
	}

	@Override
	public void translate(float x, float y) {
		centre.setX(centre.getX() + x);
		centre.setY(centre.getY() + y);
	}

	@Override
	public void scale(float sc) {
		radius *= sc;
		centre.setX(centre.getX() * sc);
		centre.setY(centre.getY() * sc);
	}

	@Override
	public void scale(Point2d centre, float sc) {
		this.translate( -centre.getX(), -centre.getY() );
		scale(sc);
		this.translate( centre.getX(), centre.getY() );
	}

	@Override
	public void scaleCOG(float sc) {
		radius *= sc;
	}

	@Override
	public Point2d getCOG() {
		return centre;
	}

	@Override
	public double calculateArea() {
		return Math.PI * radius * radius;
	}

	@Override
	public double minX() {
		return Math.round(centre.getX() - radius);
	}

	@Override
	public double minY() {
		return Math.round(centre.getY() - radius);
	}

	@Override
	public double maxX() {
		return Math.round(centre.getX() + radius);
	}

	@Override
	public double maxY() {
		return Math.round(centre.getY() + radius);
	}

	@Override
	public double getWidth() {
		return Math.round(2 * radius);
	}

	@Override
	public double getHeight() {
		return Math.round(2 * radius);
	}

	@Override
	public Shape transform(Matrix transform) {
		//TODO: could handle different cases and hand
		//back correct shape here (i.e. circle or ellipse)
		//depending on transform
		return asPolygon().transform(transform);
	}

	@Override
	public Polygon asPolygon() {
		Polygon poly = new Polygon();
		Point2dImpl[] v = new Point2dImpl[360];
		for ( int i = 0; i < 90; i++ ) 
		{
			double theta = Math.toRadians(i); 
			float xx = (float) (radius * Math.cos(theta));
			float yy = (float) (radius * Math.sin(theta));
			v[i] = new Point2dImpl(xx, yy);
			v[i+90] = new Point2dImpl(-yy, xx);
			v[i+180] = new Point2dImpl(-xx, -yy);
			v[i+270] = new Point2dImpl(yy, -xx);
		}
		
		for( int i = 0; i < 360; i++ )
			poly.points.add( v[i] );
		
		poly.translate(centre.getX(), centre.getY());
		
		return poly;
	}

	/**
	 * Set the x-ordinate of the centre of the circle.
	 * @param x The x-ordinate
	 */
	public void setX(float x) {
		this.centre.setX(x);
	}
	
	/**
	 * Set the y-ordinate of the centre of the circle.
	 * @param y The y-ordinate
	 */
	public void setY(float y) {
		this.centre.setY(y);
	}
	
	/**
	 * Set the radius of the circle.
	 * @param r The radius. 
	 */
	public void setRadius(float r) {
		this.radius = r;
	}
	
	/**
	 * @return The x-ordinate of the centre
	 */
	public float getX() {
		return centre.getX();
	}
	
	/**
	 * @return The y-ordinate of the centre
	 */
	public float getY() {
		return centre.getY();
	}
	
	/**
	 * @return The radius of the circle
	 */
	public float getRadius() {
		return radius;
	}

	@Override
	public double intersectionArea(Shape that) {
		return intersectionArea(that,100);
	}
	
	@Override
	public String toString() {
		return String.format("Circle (%f, %f, %f)",this.centre.getX(),this.centre.getY(),this.radius);
	}
	@Override
	public double intersectionArea(Shape that, int nStepsPerDimention) {
		Rectangle overlapping = this.calculateRegularBoundingBox().overlapping(that.calculateRegularBoundingBox());
		if(overlapping==null)
			return 0;
		double intersection = 0;
		double step = Math.max(overlapping.width, overlapping.height)/(double)nStepsPerDimention;
		double nReads = 0;
		for(float x = overlapping.x; x < overlapping.x + overlapping.width; x+=step){
			for(float y = overlapping.y; y < overlapping.y + overlapping.height; y+=step){
				boolean insideThis = this.isInside(new Point2dImpl(x,y));
				boolean insideThat = that.isInside(new Point2dImpl(x,y));
				nReads++;
				if(insideThis && insideThat) {
					intersection++;
				}
			}
		}
		
		return (intersection/nReads) * (overlapping.width * overlapping.height);
	}
}
