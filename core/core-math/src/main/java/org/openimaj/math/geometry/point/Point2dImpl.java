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
package org.openimaj.math.geometry.point;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;

import Jama.Matrix;

/**
 * Simple concrete implementation of a two dimensional point.
 *
 * @author Jonathon Hare
 */
public class Point2dImpl implements Point2d, Cloneable {
	/**
	 * The x-coordinate
	 */
	public float x;

	/**
	 * The y-coordinate
	 */
	public float y;

	/**
	 * Construct a Point2dImpl with the given (x, y) coordinates
	 *
	 * @param x
	 *            x-coordinate
	 * @param y
	 *            y-coordinate
	 */
	public Point2dImpl(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	/**
	 * Construct a Point2dImpl with the (x,y) coordinates given via another
	 * point.
	 *
	 * @param p
	 *            The point to copy from.
	 */
	public Point2dImpl(Point2d p)
	{
		this.copyFrom(p);
	}

	/**
	 * Construct a Point2dImpl at the origin.
	 */
	public Point2dImpl()
	{
		// do nothing
	}

	/**
	 * Construct a {@link Point2dImpl} using the first two ordinates of a
	 * {@link Coordinate}.
	 *
	 * @param coord
	 *            the {@link Coordinate}
	 */
	public Point2dImpl(Coordinate coord) {
		x = coord.getOrdinate(0).floatValue();
		y = coord.getOrdinate(1).floatValue();
	}

	/**
	 * Construct a Point2dImpl with the given (x, y) coordinates. The values
	 * will be cast to single precision.
	 *
	 * @param x
	 *            x-coordinate
	 * @param y
	 *            y-coordinate
	 */
	public Point2dImpl(double x, double y)
	{
		this.x = (float) x;
		this.y = (float) y;
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public void setX(float x) {
		this.x = x;
	}

	@Override
	public float getY() {
		return y;
	}

	@Override
	public void setY(float y) {
		this.y = y;
	}

	@Override
	public void copyFrom(Point2d p)
	{
		this.x = p.getX();
		this.y = p.getY();
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}

	@Override
	public Point2dImpl clone() {
		Point2dImpl clone;
		try {
			clone = (Point2dImpl) super.clone();
		} catch (final CloneNotSupportedException e) {
			return null;
		}
		return clone;
	}

	@Override
	public Float getOrdinate(int dimension) {
		if (dimension == 0)
			return x;
		return y;
	}

	@Override
	public int getDimensions() {
		return 2;
	}

	@Override
	public void translate(float x, float y) {
		this.x += x;
		this.y += y;
	}

	@Override
	public void translate(Point2d v) {
		this.x += v.getX();
		this.y += v.getY();
	}

	@Override
	public Point2dImpl transform(Matrix transform) {
		if (transform.getRowDimension() == 3) {
			float xt = (float) transform.get(0, 0) * getX() + (float) transform.get(0, 1) * getY()
					+ (float) transform.get(0, 2);
			float yt = (float) transform.get(1, 0) * getX() + (float) transform.get(1, 1) * getY()
					+ (float) transform.get(1, 2);
			final float zt = (float) transform.get(2, 0) * getX() + (float) transform.get(2, 1) * getY()
					+ (float) transform.get(2, 2);

			xt /= zt;
			yt /= zt;

			return new Point2dImpl(xt, yt);
		} else if (transform.getRowDimension() == 2 && transform.getColumnDimension() == 2) {
			final float xt = (float) transform.get(0, 0) * getX() + (float) transform.get(0, 1) * getY();
			final float yt = (float) transform.get(1, 0) * getX() + (float) transform.get(1, 1) * getY();

			return new Point2dImpl(xt, yt);
		} else if (transform.getRowDimension() == 2 && transform.getColumnDimension() == 3) {
			final float xt = (float) transform.get(0, 0) * getX() + (float) transform.get(0, 1) * getY()
					+ (float) transform.get(0, 2);
			final float yt = (float) transform.get(1, 0) * getX() + (float) transform.get(1, 1) * getY()
					+ (float) transform.get(1, 2);

			return new Point2dImpl(xt, yt);
		}
		throw new IllegalArgumentException("Transform matrix has unexpected size");
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Point2d))
			return false;
		final Point2d p = (Point2d) o;
		return p.getX() == this.x && p.getY() == this.y;
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	@Override
	public Point2d minus(Point2d a) {
		return new Point2dImpl(this.x - a.getX(), this.y - a.getY());
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		x = in.nextFloat();
		y = in.nextFloat();
	}

	@Override
	public String asciiHeader() {
		return "Point2d";
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		x = in.readFloat();
		y = in.readFloat();
	}

	@Override
	public byte[] binaryHeader() {
		return "PT2D".getBytes();
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.format("%f %f", x, y);
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeFloat(x);
		out.writeFloat(y);
	}

	@Override
	public Point2dImpl copy() {
		return clone();
	}

	/**
	 * Create a random point in ([0..1], [0..1]).
	 *
	 * @return random point.
	 */
	public static Point2d createRandomPoint() {
		return new Point2dImpl((float) Math.random(), (float) Math.random());
	}

	/**
	 * Create a random point in ([0..1], [0..1]) with the given random number
	 * generator.
	 *
	 * @param rng
	 *            the random number generator
	 * @return random point.
	 */
	public static Point2d createRandomPoint(Random rng) {
		return new Point2dImpl(rng.nextFloat(), rng.nextFloat());
	}

	/**
	 * @param calculateCentroid
	 * @return a point from a double array
	 */
	public static Point2d fromDoubleArray(double[] calculateCentroid) {
		return new Point2dImpl((float) calculateCentroid[0], (float) calculateCentroid[1]);
	}

	@Override
	public void setOrdinate(int dimension, Number value) {
		if (dimension == 0)
			x = value.floatValue();
		if (dimension == 1)
			y = value.floatValue();
	}
}
