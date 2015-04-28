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
import java.util.Scanner;

import Jama.Matrix;

/**
 * Simple concrete implementation of a three dimensional point.
 *
 * @author Jonathon Hare
 */
public class Point3dImpl implements Point3d, Cloneable {
	/**
	 * The x-ordinate
	 */
	public double x;

	/**
	 * The y-ordinate
	 */
	public double y;

	/**
	 * The z-ordinate
	 */
	public double z;

	/**
	 * Construct a Point3dImpl with the given (x, y, z) coordinates
	 *
	 * @param x
	 *            x-ordinate
	 * @param y
	 *            y-ordinate
	 * @param z
	 *            z-ordinate
	 */
	public Point3dImpl(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Construct a Point3dImpl with the given (x, y, z) coordinates packed into
	 * an array
	 *
	 * @param array
	 *            the coordinates ([x, y, z])
	 */
	public Point3dImpl(double[] array)
	{
		this.x = array[0];
		this.y = array[1];
		this.z = array[2];
	}

	/**
	 * Construct a Point3dImpl with the (x,y,z) coordinates given via another
	 * point.
	 *
	 * @param p
	 *            The point to copy from.
	 */
	public Point3dImpl(Point3d p)
	{
		this.copyFrom(p);
	}

	/**
	 * Construct a Point3dImpl at the origin.
	 */
	public Point3dImpl()
	{
		// do nothing
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public void setX(double x) {
		this.x = x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public void setY(double y) {
		this.y = y;
	}

	@Override
	public double getZ() {
		return z;
	}

	@Override
	public void setZ(double z) {
		this.z = z;
	}

	@Override
	public void copyFrom(Point3d p)
	{
		this.x = p.getX();
		this.y = p.getY();
		this.z = p.getZ();
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + "," + z + ")";
	}

	@Override
	public Point3dImpl clone() {
		Point3dImpl clone;
		try {
			clone = (Point3dImpl) super.clone();
		} catch (final CloneNotSupportedException e) {
			return null;
		}
		return clone;
	}

	@Override
	public Double getOrdinate(int dimension) {
		if (dimension == 0)
			return x;
		if (dimension == 1)
			return y;
		return z;
	}

	@Override
	public int getDimensions() {
		return 3;
	}

	@Override
	public void translate(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
	}

	@Override
	public void translate(Point3d v) {
		this.x += v.getX();
		this.y += v.getY();
		this.z += v.getZ();
	}

	@Override
	public Point3dImpl transform(Matrix transform) {
		if (transform.getRowDimension() == 4) {
			double xt = transform.get(0, 0) * getX() + transform.get(0, 1) * getY() + transform.get(0, 2) * getZ()
					+ transform.get(0, 3);
			double yt = transform.get(1, 0) * getX() + transform.get(1, 1) * getY() + transform.get(1, 2) * getZ()
					+ transform.get(1, 3);
			double zt = transform.get(2, 0) * getX() + transform.get(2, 1) * getY() + transform.get(2, 2) * getZ()
					+ transform.get(2, 3);
			final double ft = transform.get(3, 0) * getX() + transform.get(3, 1) * getY() + transform.get(3, 2) * getZ()
					+ transform.get(3, 3);

			xt /= ft;
			yt /= ft;
			zt /= ft;

			return new Point3dImpl(xt, yt, zt);
		}
		throw new IllegalArgumentException("Transform matrix has unexpected size");
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Point3d))
			return false;
		final Point3d p = (Point3d) o;
		return p.getX() == this.x && p.getY() == this.y && p.getZ() == this.getZ();
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	@Override
	public Point3d minus(Point3d a) {
		return new Point3dImpl(this.x - a.getX(), this.y - a.getY(), this.z - a.getZ());
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		x = in.nextDouble();
		y = in.nextDouble();
		z = in.nextDouble();
	}

	@Override
	public String asciiHeader() {
		return "Point3d";
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		x = in.readDouble();
		y = in.readDouble();
		z = in.readDouble();
	}

	@Override
	public byte[] binaryHeader() {
		return "PT3D".getBytes();
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.format("%f %f %f", x, y, z);
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeDouble(x);
		out.writeDouble(y);
		out.writeDouble(z);
	}

	@Override
	public Point3dImpl copy() {
		return clone();
	}

	/**
	 * Create a random point in ([0..1], [0..1], [0..1]).
	 *
	 * @return random point.
	 */
	public static Point3d createRandomPoint() {
		return new Point3dImpl(Math.random(), Math.random(), Math.random());
	}

	@Override
	public void setOrdinate(int dimension, Number value) {
		if (dimension == 0)
			x = value.floatValue();
		if (dimension == 1)
			y = value.floatValue();
		if (dimension == 2)
			z = value.floatValue();
	}
}
