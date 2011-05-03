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
 * A rectangle shape
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class Rectangle implements Shape {
	private static final long serialVersionUID = 1L;
	
	/** The x-coordinate of the top-left of the rectangle */
	public float x;
	
	/** The y-coordinate of the top-left of the rectangle */
	public float y;
	
	/** The width of the rectangle */
	public float width;
	
	/** The height of the rectangle */
	public float height;
	
	/**
	 * Construct a Rectangle with the given parameters. 
	 * @param x x-coordinate of top-left 
	 * @param y y-coordinate of top-left
	 * @param width width
	 * @param height height
	 */
	public Rectangle(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Construct a Rectangle with the given parameters. 
	 * @param topLeft top-left corner
	 * @param bottomRight bottom-right corner 
	 */
	public Rectangle(Point2d topLeft, Point2d bottomRight) {
		x = topLeft.getX();
		y = topLeft.getY();
		width = bottomRight.getX() - x;
		height = bottomRight.getY() - y;
	}

	@Override
	public boolean isInside(Point2d point) {
		float px = point.getX();
		float py = point.getY();
		
		if (px>=x && px<=x+width && py >= y && py<=y+height) return true;
		
		return false;
	}

	@Override
	public int[] calculateRegularBoundingBox() {
		return new int[] {Math.round(x), Math.round(y), Math.round(width), Math.round(height)};
	}

	@Override
	public void translate(float x, float y) {
		this.x += x;
		this.y += y;
	}

	@Override
	public void scale(float sc) {
		x *= sc;
		y *= sc;
		width *= sc;
		height *= sc;
	}

	@Override
	public void scale(Point2d centre, float sc) {
		translate(-centre.getX(), -centre.getY());
		scale(sc);
		translate(centre.getX(), centre.getY());
	}

	@Override
	public void scaleCOG(float sc) {
		Point2d centre = this.getCOG();
		translate(-centre.getX(), -centre.getY());
		scale(sc);
		translate(centre.getX(), centre.getY());
	}

	@Override
	public Point2d getCOG() {
		return new Point2dImpl(x+width/2, y+height/2);
	}

	@Override
	public double calculateArea() {
		return width*height;
	}

	@Override
	public double minX() {
		return x;
	}

	@Override
	public double minY() {
		return y;
	}

	@Override
	public double maxX() {
		return x+width;
	}

	@Override
	public double maxY() {
		return y+height;
	}

	@Override
	public double getWidth() {
		return width;
	}

	@Override
	public double getHeight() {
		return height;
	}

	@Override
	public Shape transform(Matrix transform) {
		//TODO: could handle different cases and hand
		//back correct shape here depending on transform
		return asPolygon().transform(transform);
	}

	@Override
	public Polygon asPolygon() {
		Polygon polygon = new Polygon();
		polygon.vertices.add(new Point2dImpl(x, y));
		polygon.vertices.add(new Point2dImpl(x+width, y));
		polygon.vertices.add(new Point2dImpl(x+width, y+height));
		polygon.vertices.add(new Point2dImpl(x, y+height));
		return polygon;
	}
	
	/**
	 * Set the position and size of this rectangle
	 * @param x x-coordinate of top-left 
	 * @param y y-coordinate of top-left
	 * @param width width
	 * @param height height
	 */
	public void setBounds(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public String toString() {
		return String.format("Rectangle[x=%2.2f,y=%2.2f,width=%2.2f,height=%2.2f]", x, y, width, height);
	}
}
