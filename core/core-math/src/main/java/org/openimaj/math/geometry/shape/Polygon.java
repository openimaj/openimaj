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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

import Jama.Matrix;

/**
 * A polygon, modeled as a list of vertices.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class Polygon implements Shape {
	private static final long serialVersionUID = 1L;

	List<Point2d> vertices = new ArrayList<Point2d>();

	/**
	 * Construct a Polygon from vertices
	 * @param vertices the vertices
	 */
	public Polygon(Point2d... vertices) {
		for (Point2d v : vertices)
			this.vertices.add(v);
	}

	/**
	 * Construct a Polygon from vertices
	 * @param vertices the vertices
	 */
	public Polygon(Collection<? extends Point2d> vertices) {
		this( vertices, false );
	}

	/**
	 * Construct a Polygon from the vertices, possibly 
	 * copying the vertices first
	 * @param vertices the vertices
	 * @param copy should the vertices be copied
	 */
	public Polygon( Collection<? extends Point2d> vertices, boolean copy ) {
		if( !copy )
			this.vertices.addAll( vertices );
		else
		{
			for( Point2d p : vertices )
				this.vertices.add( new Point2dImpl( p.getX(), p.getY() ) );
		}
	}

	void rotate(Point2d point, Point2d origin, double angle) {
		double X = origin.getX() + ((point.getX() - origin.getX()) * Math.cos(angle) -
				(point.getY() - origin.getY()) * Math.sin(angle));

		double Y = origin.getY() + ((point.getX() - origin.getX()) * Math.sin(angle) +
				(point.getY() - origin.getY()) * Math.cos(angle));

		point.setX((float) X);
		point.setY((float) Y);
	}

	/**
	 * Rotate the polygon about the given origin with the given angle (in radians)
	 * @param origin the origin of the rotation
	 * @param angle the angle in radians
	 */
	public void rotate(Point2d origin, double angle) {
		for (Point2d p : vertices)
			rotate(p, origin, angle);
	}

	/**
	 * Rotate the polygon about (0,0) with the given angle (in radians)
	 * @param angle the angle in radians
	 */
	public void rotate(double angle) {
		this.rotate( new Point2dImpl(0,0), angle );
	}

	/**
	 * Get the vertices of the polygon
	 * @return the vertices
	 */
	public List<Point2d> getVertices() {
		return vertices;
	}

	/**
	 * Get the number of vertices
	 * @return the number of vertices
	 */
	public int nVertices() {
		if (isClosed())
			return vertices.size() - 1;
		return vertices.size();
	}

	/**
	 * Is the polygon closed (i.e. is the last vertex equal to the first)?
	 * @return true if closed; false if open
	 */
	public boolean isClosed() {
		if (vertices.get(0) == vertices.get(vertices.size()-1))
			return true;
		return false;
	}

	/**
	 * Close the polygon if it's not already closed
	 */
	public void close() {
		if (!isClosed())
			vertices.add(vertices.get(0));
	}

	/**
	 * Open the polygon if it's closed
	 */
	public void open() {
		if (isClosed())
			vertices.remove(vertices.get(vertices.size()-1));
	}

	/**
	 * Test whether the point p is inside the polygon using the winding rule algorithm.
	 * @param point the point
	 * @return true if the point is inside; false otherwise
	 */
	@Override
	public boolean isInside(Point2d point) {
		boolean isClosed = isClosed();
		if (!isClosed) close();

		int j=nVertices()-1 ;
		boolean isOdd=false;

		for (int i=0; i<nVertices(); i++) {
			if (vertices.get(i).getY() < point.getY() && vertices.get(j).getY() >= point.getY() || 
					vertices.get(j).getY() < point.getY() && vertices.get(i).getY() >= point.getY()) {
				if (vertices.get(i).getX() + (point.getY()-vertices.get(i).getY()) / 
						(vertices.get(j).getY()-vertices.get(i).getY())*(vertices.get(j).getX()-vertices.get(i).getX()) < point.getX()) {
					isOdd=!isOdd; 
				}
			}
			j=i; 
		}

		if (!isClosed) open();

		return isOdd;
	}

	/**
	 * Compute the regular (oriented to the axes) bounding box
	 * of the polygon.
	 * 
	 * @return the regular bounding box as [x,y,width,height]
	 */
	@Override
	public Rectangle calculateRegularBoundingBox() {
		int xmin=Integer.MAX_VALUE, xmax=0, ymin=Integer.MAX_VALUE, ymax=0;

		for (Point2d p : vertices) {
			if (p.getX() < xmin) xmin = (int) Math.floor(p.getX());
			if (p.getX() > xmax) xmax = (int) Math.ceil(p.getX());
			if (p.getY() < ymin) ymin = (int) Math.floor(p.getY());
			if (p.getY() > ymax) ymax = (int) Math.ceil(p.getY());
		}

		return new Rectangle(xmin, ymin, xmax-xmin, ymax-ymin);
	}

	/**
	 * Translate the polygons position
	 *  
	 * @param x x-translation
	 * @param y y-translation
	 */
	@Override
	public void translate(float x, float y) {
		for (Point2d p : vertices) { 
			p.setX(p.getX() + x);
			p.setY(p.getY() + y);
		}
	}

	@Override
	public Polygon clone() {
		Polygon clone = new Polygon();

		for (Point2d p : vertices)
			clone.vertices.add(p.clone());

		return clone;
	}

	/**
	 * Scale the polygon by the given amount about (0,0). Scalefactors
	 * between 0 and 1 shrink the polygon. 
	 * @param sc the scale factor.
	 */
	@Override
	public void scale(float sc) {
		for (Point2d p : vertices) {
			p.setX(p.getX() * sc);
			p.setY(p.getY() * sc);
		}
	}

	/**
	 * Scale the polygon by the given amount about the given point. 
	 * Scalefactors between 0 and 1 shrink the polygon.
	 * @param centre the centre of the scaling operation
	 * @param sc the scale factor
	 */
	@Override
	public void scale(Point2d centre, float sc) {
		this.translate( -centre.getX(), -centre.getY() );
		for (Point2d p : vertices) {
			p.setX(p.getX() * sc);
			p.setY(p.getY() * sc);
		}
		this.translate( centre.getX(), centre.getY() );
	}

	/**
	 * Scale the polygon about its centre of gravity.
	 * Scalefactors between 0 and 1 shrink the polygon.
	 * @param sc the scale factor
	 */
	@Override
	public void scaleCOG( float sc )
	{
		Point2d cog = getCOG();
		this.translate( -cog.getX(), -cog.getY() );
		this.scale( sc );
		this.translate( cog.getX(), cog.getY() );
	}

	/**
	 * Get the centre of gravity of the polygon
	 * @return the centre of gravity of the polygon
	 */
	@Override
	public Point2d getCOG()
	{
		float xSum = 0;
		float ySum = 0;

		for( Point2d p : vertices )
		{
			xSum += p.getX();
			ySum += p.getY();
		}

		xSum /= vertices.size();
		ySum /= vertices.size();

		return new Point2dImpl( xSum, ySum );
	}

	/**
	 * 	Calculates the difference between two polygons
	 * 	and returns a new polygon. It assumes that the given
	 * 	polygon and this polygon have the same number of
	 * 	vertices.
	 *  @param p the polygon to subtract.
	 *  @return the difference polygon 
	 */
	public Polygon difference( Polygon p )
	{
		List<Point2d> v = new ArrayList<Point2d>();
		for( int i = 0; i < vertices.size(); i++ )
			v.add( new Point2dImpl( 
					vertices.get(i).getX() - p.getVertices().get(i).getX(),
					vertices.get(i).getY() - p.getVertices().get(i).getY() ) );
		return new Polygon( v );
	}

	/**
	 * Calculate the area of the polygon
	 * @return the area of the polygon
	 */
	@Override
	public double calculateArea() {
		boolean closed = isClosed();
		double area = 0;

		if (!closed) close();

		for (int k=0; k<vertices.size()-1; k++) {
			float ik = vertices.get(k).getX();
			float jk = vertices.get(k).getY();
			float ik1 = vertices.get(k+1).getX();
			float jk1 = vertices.get(k+1).getY();

			area += ik*jk1 - ik1*jk;
		}

		if (!closed) open();

		return 0.5 * Math.abs(area);
	}

	/**
	 * @return the minimum x-ordinate of all vertices
	 */
	@Override
	public double minX() { double min = Double.MAX_VALUE; for( Point2d p : vertices ) min = (p.getX() < min)?p.getX():min; return min; }

	/**
	 * @return the minimum y-ordinate of all vertices
	 */
	@Override
	public double minY() { double min = Double.MAX_VALUE; for( Point2d p : vertices ) min = (p.getY() < min)?p.getY():min; return min; }

	/**
	 * @return the maximum x-ordinate of all vertices
	 */
	@Override
	public double maxX() { double max = Double.MIN_VALUE; for( Point2d p : vertices ) max = (p.getX() > max)?p.getX():max; return max; }

	/**
	 * @return the maximum y-ordinate of all vertices
	 */
	@Override
	public double maxY() { double max = Double.MIN_VALUE; for( Point2d p : vertices ) max = (p.getY() > max)?p.getY():max; return max; }

	/**
	 * @return the width of the regular bounding box 
	 */
	@Override
	public double getWidth() { return maxX()-minX(); }

	/**
	 * @return the height of the regular bounding box
	 */
	@Override
	public double getHeight() { return maxY()-minY(); }

	/**
	 * Apply a 3x3 transform matrix to a copy of the polygon
	 * and return it
	 * @param transform 3x3 transform matrix
	 * @return the transformed polygon
	 */
	@Override
	public Polygon transform(Matrix transform) {
		List<Point2d> newVertices = new ArrayList<Point2d>();

		for (Point2d p : vertices) {
			Matrix p1 = new Matrix(3,1);
			p1.set(0,0, p.getX());
			p1.set(1,0, p.getY());
			p1.set(2,0, 1);

			Matrix p2_est = transform.times(p1);

			Point2d out = new Point2dImpl((float)(p2_est.get(0,0) / p2_est.get(2, 0)), (float)(p2_est.get(1,0) / p2_est.get(2, 0)));

			newVertices.add(out);
		}

		return new Polygon(newVertices);
	}

	@Override
	public Polygon asPolygon() {
		return this;
	}
}
