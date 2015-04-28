package org.openimaj.math.geometry.point;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import Jama.Matrix;

/**
 * Abstract base for {@link Point2d} implementations that retains the underlying
 * precision.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public abstract class AbstractPoint2d implements Point2d {
	@Override
	public int getDimensions() {
		return 2;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Point2d))
			return false;
		final Point2d p = (Point2d) o;
		return p.getOrdinate(0).equals(getOrdinate(0)) && p.getOrdinate(1).equals(getOrdinate(1));
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		setOrdinate(0, in.nextDouble());
		setOrdinate(1, in.nextDouble());
	}

	@Override
	public String asciiHeader() {
		return "Point2d";
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		setOrdinate(0, in.readDouble());
		setOrdinate(1, in.readDouble());
	}

	@Override
	public byte[] binaryHeader() {
		return "PT2DD".getBytes();
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.format("%f %f", getOrdinate(0).doubleValue(), getOrdinate(1).doubleValue());
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeDouble(getOrdinate(0).doubleValue());
		out.writeDouble(getOrdinate(1).doubleValue());
	}

	@Override
	public float getX() {
		return getOrdinate(0).floatValue();
	}

	@Override
	public void setX(float x) {
		setOrdinate(0, x);
	}

	@Override
	public float getY() {
		return getOrdinate(1).floatValue();
	}

	@Override
	public void setY(float y) {
		setOrdinate(1, y);
	}

	@Override
	public void copyFrom(Point2d p) {
		setOrdinate(0, p.getOrdinate(0));
		setOrdinate(1, p.getOrdinate(1));
	}

	@Override
	public void translate(float x, float y) {
		setOrdinate(0, getOrdinate(0).doubleValue() + x);
		setOrdinate(0, getOrdinate(1).doubleValue() + y);
	}

	@Override
	public Point2d transform(Matrix transform) {
		double xt, yt, zt;
		if (transform.getRowDimension() == 3) {
			xt = transform.get(0, 0) * getX() + transform.get(0, 1) * getY()
					+ transform.get(0, 2);
			yt = transform.get(1, 0) * getX() + transform.get(1, 1) * getY()
					+ transform.get(1, 2);
			zt = transform.get(2, 0) * getX() + transform.get(2, 1) * getY()
					+ transform.get(2, 2);

			xt /= zt;
			yt /= zt;
		} else if (transform.getRowDimension() == 2 && transform.getColumnDimension() == 2) {
			xt = transform.get(0, 0) * getX() + transform.get(0, 1) * getY();
			yt = transform.get(1, 0) * getX() + transform.get(1, 1) * getY();
		} else if (transform.getRowDimension() == 2 && transform.getColumnDimension() == 3) {
			xt = transform.get(0, 0) * getX() + transform.get(0, 1) * getY() + transform.get(0, 2);
			yt = transform.get(1, 0) * getX() + transform.get(1, 1) * getY() + transform.get(1, 2);
		} else {
			throw new IllegalArgumentException("Transform matrix has unexpected size");
		}

		final Point2d cpy = copy();
		cpy.setOrdinate(0, xt);
		cpy.setOrdinate(1, yt);
		return cpy;
	}

	@Override
	public Point2d minus(Point2d a) {
		final Point2d cpy = copy();
		cpy.setOrdinate(0, getOrdinate(0).doubleValue() - a.getOrdinate(0).doubleValue());
		cpy.setOrdinate(1, getOrdinate(1).doubleValue() - a.getOrdinate(1).doubleValue());
		return cpy;
	}

	@Override
	public void translate(Point2d v) {
		setOrdinate(0, getOrdinate(0).doubleValue() + v.getOrdinate(0).doubleValue());
		setOrdinate(1, getOrdinate(1).doubleValue() + v.getOrdinate(1).doubleValue());
	}
}
