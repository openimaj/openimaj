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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.openimaj.io.ReadWriteable;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

import Jama.Matrix;

/**
 * A rectangle shape oriented to the axes. For non-oriented
 * versions, use a polygon.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class Rectangle implements Shape, ReadWriteable {
	/** The x-coordinate of the top-left of the rectangle */
	public float x;
	
	/** The y-coordinate of the top-left of the rectangle */
	public float y;
	
	/** The width of the rectangle */
	public float width;
	
	/** The height of the rectangle */
	public float height;
	
	/**
	 * Construct a unit rectangle
	 */
	public Rectangle() {
		this(0,0,1,1);
	}
	
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
	public Rectangle calculateRegularBoundingBox() {
		return new Rectangle(Math.round(x), Math.round(y), Math.round(width), Math.round(height));
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
	
	public boolean isOverlapping(Rectangle other){
		float left = x; float right = x + width; float top = y; float bottom = y + height;
		float otherleft = other.x; float otherright = other.x + other.width; float othertop = other.y; float otherbottom = other.y + height;
		return !(left > otherright || right < otherleft || top > otherbottom || bottom < othertop);
	}
	
	public Rectangle overlapping(Rectangle other){
		if(!isOverlapping(other))return null;
		float left = x; float right = x + width; float top = y; float bottom = y + height;
		float otherleft = other.x; float otherright = other.x + other.width; float othertop = other.y; float otherbottom = other.y + height;
		float overlapleft = Math.max(left, otherleft);
		float overlaptop = Math.max(top, othertop);
		float overlapwidth = Math.min(right,otherright) - overlapleft;
		float overlapheight = Math.min(bottom, otherbottom) - overlaptop;
		return new Rectangle(overlapleft,overlaptop,overlapwidth,overlapheight);
	}
	@Override
	public double intersectionArea(Shape that) {
		return intersectionArea(that,1);
	}

	@Override
	public double intersectionArea(Shape that, int nStepsPerDimention) {
		Rectangle overlapping = this.calculateRegularBoundingBox().overlapping(that.calculateRegularBoundingBox());
		if(overlapping == null) return 0;
		if(that instanceof Rectangle){
			// Special case
			return overlapping.calculateArea();
		}
		else{
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

	@Override
	public void readASCII(Scanner in) throws IOException {
		x = in.nextFloat();
		y = in.nextFloat();
		width = in.nextFloat();
		height = in.nextFloat();
	}

	@Override
	public String asciiHeader() {
		return "Rectangle";
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		x = in.readFloat();
		y = in.readFloat();
		width = in.readFloat();
		height = in.readFloat();
	}

	@Override
	public byte[] binaryHeader() {
		return "Rectangle".getBytes();
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.write(String.format("%f %f %f %f\n", x, y, width, height));
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeFloat(x);
		out.writeFloat(y);
		out.writeFloat(width);
		out.writeFloat(height);
	}
}
