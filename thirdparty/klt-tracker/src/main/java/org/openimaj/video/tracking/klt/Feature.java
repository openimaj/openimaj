/**
 * This source code file is part of a direct port of Stan Birchfield's implementation
 * of a Kanade-Lucas-Tomasi feature tracker. The original implementation can be found
 * here: http://www.ces.clemson.edu/~stb/klt/
 *
 * As per the original code, the source code is in the public domain, available
 * for both commercial and non-commercial use.
 */
package org.openimaj.video.tracking.klt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.openimaj.math.geometry.point.Point2d;

import Jama.Matrix;

/**
 * A tracked feature
 *
 * @author Stan Birchfield
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class Feature implements Point2d, Cloneable {
	/**
	 * x ordinate of feature
	 */
	public float x;

	/**
	 * y ordinate of feature
	 */
	public float y;

	/**
	 * value of feature
	 */
	public int val;

	/* for affine mapping */
	// public FImage aff_img;
	// public FImage aff_img_gradx;
	// public FImage aff_img_grady;
	// public float aff_x;
	// public float aff_y;
	// public float aff_Axx;
	// public float aff_Ayx;
	// public float aff_Axy;
	// public float aff_Ayy;

	/**
	 * Convert to string representation with the given format
	 * 
	 * @param format
	 * @param type
	 * @return formatted string
	 */
	public String toString(String format, String type) {
		assert (type.equals("f") || type.equals("d"));
		String s = "";

		if (type.equals("f"))
			s += String.format(format, x, y, val);
		else if (type.equals("d")) {
			/* Round x & y to nearest integer, unless negative */
			float _x = x;
			float _y = y;
			if (_x >= 0.0)
				_x += 0.5;
			if (_y >= 0.0)
				_y += 0.5;
			s += String.format(format, (int) x, (int) y, val);
		}

		return s;
	}

	/**
	 * Write feature as binary data
	 * 
	 * @param os
	 * @throws IOException
	 */
	public void writeFeatureBin(DataOutputStream os) throws IOException {
		os.writeFloat(x);
		os.writeFloat(y);
		os.writeInt(val);
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public float getY() {
		return y;
	}

	@Override
	public void setX(float x) {
		this.x = x;
	}

	@Override
	public void setY(float y) {
		this.y = y;
	}

	@Override
	public Feature clone() {
		final Feature f = new Feature();

		f.x = x;
		f.y = y;
		f.val = val;
		// f.aff_img = aff_img;
		// f.aff_img_gradx = aff_img_gradx;
		// f.aff_img_grady = aff_img_grady;
		// f.aff_x = aff_x;
		// f.aff_y = aff_y;
		// f.aff_Axx = aff_Axx;
		// f.aff_Ayx = aff_Ayx;
		// f.aff_Axy = aff_Axy;
		// f.aff_Ayy = aff_Ayy;

		return f;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Feature))
			return false;

		if (((Feature) o).x == x && ((Feature) o).y == y && ((Feature) o).val == val)
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = (int) ((31 * hash) + x);
		hash = (int) ((31 * hash) + y);
		hash = ((31 * hash) + val);
		// hash = (int) ((31 * hash) + aff_img;
		// hash = (int) ((31 * hash) + aff_img_gradx;
		// hash = (int) ((31 * hash) + aff_img_grady;
		// hash = (int) ((31 * hash) + aff_x;
		// hash = (int) ((31 * hash) + aff_y;
		// hash = (int) ((31 * hash) + aff_Axx;
		// hash = (int) ((31 * hash) + aff_Ayx;
		// hash = (int) ((31 * hash) + aff_Axy;
		// hash = (int) ((31 * hash) + aff_Ayy;

		return hash;
	}

	@Override
	public String toString() {
		return "Feature(" + x + ", " + y + ", " + val + ")";
	}

	@Override
	public void copyFrom(Point2d p)
	{
		setX(p.getX());
		setY(p.getY());
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
	public Feature transform(Matrix transform) {
		if (transform.getRowDimension() == 3) {
			float xt = (float) transform.get(0, 0) * getX() + (float) transform.get(0, 1) * getY()
					+ (float) transform.get(0, 2);
			float yt = (float) transform.get(1, 0) * getX() + (float) transform.get(1, 1) * getY()
					+ (float) transform.get(1, 2);
			final float zt = (float) transform.get(2, 0) * getX() + (float) transform.get(2, 1) * getY()
					+ (float) transform.get(2, 2);

			xt /= zt;
			yt /= zt;

			final Feature f = this.clone();
			f.x = xt;
			f.y = yt;
			return f;
		} else if (transform.getRowDimension() == 2) {
			final float xt = (float) transform.get(0, 0) * getX() + (float) transform.get(0, 1) * getY();
			final float yt = (float) transform.get(1, 0) * getX() + (float) transform.get(1, 1) * getY();

			final Feature f = this.clone();
			f.x = xt;
			f.y = yt;
			return f;
		}
		throw new IllegalArgumentException("Transform matrix has unexpected size");
	}

	@Override
	public Point2d minus(Point2d a) {
		final Point2d p = this.clone();
		p.setX(this.getX() - a.getX());
		p.setY(this.getY() - a.getY());
		return p;
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		x = in.nextFloat();
		y = in.nextFloat();
		val = in.nextInt();
	}

	@Override
	public String asciiHeader() {
		return this.getClass().getName();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		x = in.readFloat();
		y = in.readFloat();
		val = in.readInt();
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.format("%f %f %d", x, y, val);
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeFloat(x);
		out.writeFloat(y);
		out.writeInt(val);
	}

	@Override
	public void translate(Point2d v) {
		this.translate(v.getX(), v.getY());
	}

	@Override
	public Feature copy() {
		return clone();
	}

	@Override
	public void setOrdinate(int dimension, Number value) {
		if (dimension == 0)
			x = value.floatValue();
		if (dimension == 1)
			y = value.floatValue();
	}
}
