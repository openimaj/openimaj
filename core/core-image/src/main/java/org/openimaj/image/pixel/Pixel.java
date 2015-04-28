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
package org.openimaj.image.pixel;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.openimaj.math.geometry.point.Point2d;

import Jama.Matrix;

/**
 * Represents a pixel within an image, storing its coordinates. Provides helper
 * methods for rounding non-integer values to pixel coordinates.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class Pixel implements Point2d, Cloneable {
	/** The x-coordinate of this pixel */
	public int x;

	/** The y-coordinate of this pixel */
	public int y;

	/**
	 * Construct a pixel with the given coordinates.
	 *
	 * @param x
	 *            The x-coordinate of the pixel
	 * @param y
	 *            The y-coordinate of the pixel
	 */
	public Pixel(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Construct a pixel at the origin
	 */
	public Pixel() {
		this(0, 0);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Pixel))
			return false;

		if (((Pixel) o).x == x && ((Pixel) o).y == y)
			return true;
		return false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 17;
		hash = (31 * hash) + x;
		hash = (31 * hash) + y;
		return hash;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.math.geometry.point.Point2d#getX()
	 */
	@Override
	public float getX() {
		return x;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.math.geometry.point.Point2d#getY()
	 */
	@Override
	public float getY() {
		return y;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.math.geometry.point.Point2d#setX(float)
	 */
	@Override
	public void setX(float x) {
		this.x = Math.round(x);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.math.geometry.point.Point2d#setY(float)
	 */
	@Override
	public void setY(float y) {
		this.y = Math.round(y);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pixel clone() {
		return new Pixel(x, y);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.math.geometry.point.Point2d#copyFrom(org.openimaj.math.geometry.point.Point2d)
	 */
	@Override
	public void copyFrom(Point2d p) {
		this.setX(p.getX());
		this.setY(p.getY());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.math.geometry.point.Coordinate#getOrdinate(int)
	 */
	@Override
	public Integer getOrdinate(int dimension) {
		if (dimension == 0)
			return x;
		return y;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.math.geometry.point.Coordinate#getDimensions()
	 */
	@Override
	public int getDimensions() {
		return 2;
	}

	@Override
	public void translate(float x, float y) {
		this.x = Math.round(this.x + x);
		this.y = Math.round(this.y + y);
	}

	@Override
	public Pixel transform(Matrix transform) {
		if (transform.getRowDimension() == 3) {
			float xt = (float) transform.get(0, 0) * getX() + (float) transform.get(0, 1) * getY()
					+ (float) transform.get(0, 2);
			float yt = (float) transform.get(1, 0) * getX() + (float) transform.get(1, 1) * getY()
					+ (float) transform.get(1, 2);
			final float zt = (float) transform.get(2, 0) * getX() + (float) transform.get(2, 1) * getY()
					+ (float) transform.get(2, 2);

			xt /= zt;
			yt /= zt;

			return new Pixel(Math.round(xt), Math.round(yt));
		} else if (transform.getRowDimension() == 2) {
			final float xt = (float) transform.get(0, 0) * getX() + (float) transform.get(0, 1) * getY();
			final float yt = (float) transform.get(1, 0) * getX() + (float) transform.get(1, 1) * getY();

			return new Pixel(Math.round(xt), Math.round(yt));
		}
		throw new IllegalArgumentException("Transform matrix has unexpected size");
	}

	/**
	 * Inplace transform the point by the given matrix.
	 *
	 * @param transform
	 *            the transform
	 * @return this
	 */
	public Pixel transformInplace(Matrix transform) {
		if (transform.getRowDimension() == 3) {
			float xt = (float) transform.get(0, 0) * getX() + (float) transform.get(0, 1) * getY()
					+ (float) transform.get(0, 2);
			float yt = (float) transform.get(1, 0) * getX() + (float) transform.get(1, 1) * getY()
					+ (float) transform.get(1, 2);
			final float zt = (float) transform.get(2, 0) * getX() + (float) transform.get(2, 1) * getY()
					+ (float) transform.get(2, 2);

			xt /= zt;
			yt /= zt;

			this.x = Math.round(xt);
			this.y = Math.round(yt);
			return this;
		} else if (transform.getRowDimension() == 2) {
			final float xt = (float) transform.get(0, 0) * getX() + (float) transform.get(0, 1) * getY();
			final float yt = (float) transform.get(1, 0) * getX() + (float) transform.get(1, 1) * getY();

			this.x = Math.round(xt);
			this.y = Math.round(yt);
			return this;
		}
		throw new IllegalArgumentException("Transform matrix has unexpected size");
	}

	@Override
	public Point2d minus(Point2d a) {
		return new Pixel(this.x - (int) a.getX(), this.y - (int) a.getY());
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		x = in.nextInt();
		y = in.nextInt();
	}

	@Override
	public String asciiHeader() {
		return "Pixel";
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		x = in.readInt();
		y = in.readInt();
	}

	@Override
	public byte[] binaryHeader() {
		return "PX".getBytes();
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.format("%d %d", x, y);
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(x);
		out.writeInt(y);
	}

	@Override
	public void translate(Point2d v) {
		this.translate(v.getX(), v.getY());
	}

	@Override
	public Pixel copy() {
		return clone();
	}

	@Override
	public void setOrdinate(int dimension, Number value) {
		if (dimension == 0)
			x = value.intValue();
		if (dimension == 1)
			y = value.intValue();
	}
}
