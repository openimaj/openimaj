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
package org.openimaj.image.feature.local.keypoints;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.openimaj.feature.ByteFV;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.io.VariableLength;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.ScaleSpacePoint;

import Jama.Matrix;
import cern.jet.random.Normal;

/**
 * A local interest point with a location, scale, orientation and associated
 * feature. The feature is stored as an array of bytes.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class Keypoint
implements
Serializable,
ScaleSpacePoint,
LocalFeature<KeypointLocation, ByteFV>,
VariableLength,
Cloneable
{
	static final long serialVersionUID = 1234554345;

	/**
	 * Default length of standard SIFT features.
	 */
	private final static int DEFAULT_LENGTH = 128;

	/**
	 * keypoint feature descriptor (i.e. SIFT)
	 */
	public byte[] ivec;

	/**
	 * dominant orientation of keypoint
	 */
	public float ori;

	/**
	 * scale of keypoint
	 */
	public float scale;

	/**
	 * x-position of keypoint
	 */
	public float x;

	/**
	 * y-position of keypoint
	 */
	public float y;

	/**
	 * Construct with the default feature vector length for SIFT (128).
	 */
	public Keypoint() {
		this.ivec = new byte[DEFAULT_LENGTH];
	}

	/**
	 * Construct with the given feature vector length.
	 *
	 * @param length
	 *            the length of the feature vector
	 */
	public Keypoint(int length) {
		if (length < 0)
			length = DEFAULT_LENGTH;
		this.ivec = new byte[length];
	}

	/**
	 * Construct with the given parameters.
	 *
	 * @param x
	 *            the x-ordinate of the keypoint
	 * @param y
	 *            the y-ordinate of the keypoint
	 * @param ori
	 *            the orientation of the keypoint
	 * @param scale
	 *            the scale of the keypoint
	 * @param ivec
	 *            the feature vector of the keypoint
	 */
	public Keypoint(float x, float y, float ori, float scale, byte[] ivec) {
		this.x = x;
		this.y = y;
		this.ori = ori;
		this.scale = scale;
		this.ivec = ivec;
	}

	/**
	 * Construct by copying from another {@link Keypoint}
	 *
	 * @param k
	 *            the {@link Keypoint} to copy from
	 */
	public Keypoint(Keypoint k) {
		this(k.x, k.y, k.ori, k.scale, Arrays.copyOf(k.ivec, k.ivec.length));
	}

	@Override
	public Float getOrdinate(int dimension) {
		if (dimension == 0)
			return x;
		if (dimension == 1)
			return y;
		if (dimension == 2)
			return scale;
		return null;
	}

	@Override
	public int getDimensions() {
		return 3;
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
	public float getScale() {
		return scale;
	}

	@Override
	public void setScale(float scale) {
		this.scale = scale;
	}

	@Override
	public String toString() {
		return ("Keypoint(" + this.x + ", " + this.y + ", " + this.scale + ", " + this.ori + ")");
	}

	/**
	 * Test whether the location of this {@link Keypoint} and another
	 * {@link Keypoint} is the same.
	 *
	 * @param obj
	 *            the other keypoint
	 * @return true if the locations match; false otherwise.
	 */
	public boolean locationEquals(Object obj) {
		if (obj instanceof Keypoint) {
			final Keypoint kobj = (Keypoint) obj;

			if (kobj.x == x && kobj.y == y && kobj.scale == scale)
				return true;
		}

		return super.equals(obj);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Keypoint) {
			final Keypoint kobj = (Keypoint) obj;

			if (kobj.x == x && kobj.y == y && kobj.scale == scale && Arrays.equals(ivec, kobj.ivec))
				return true;
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + Float.floatToIntBits(y);
		hash = hash * 31 + Float.floatToIntBits(x);
		hash = hash * 31 + Float.floatToIntBits(scale);
		return hash;
	}

	@Override
	public Keypoint clone() {
		final Keypoint clone = new Keypoint();

		clone.x = x;
		clone.ori = ori;
		clone.y = y;
		clone.scale = scale;

		clone.ivec = new byte[ivec.length];
		System.arraycopy(ivec, 0, clone.ivec, 0, ivec.length);

		return clone;
	}

	@Override
	public void copyFrom(Point2d p) {
		setX(p.getX());
		setY(p.getY());
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		getLocation().writeBinary(out);
		out.write(this.ivec);
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		/* Output data for the keypoint. */
		getLocation().writeASCII(out);
		for (int i = 0; i < ivec.length; i++) {
			if (i > 0 && i % 20 == 0)
				out.println();
			out.print(" " + (ivec[i] + 128));
		}
		out.println();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		final KeypointLocation l = getLocation();
		l.readBinary(in);
		setLocation(l);

		in.readFully(ivec);
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		final KeypointLocation l = getLocation();
		l.readASCII(in);
		setLocation(l);

		int i = 0;
		while (i < ivec.length) {
			final String line = in.nextLine();
			final StringTokenizer st = new StringTokenizer(line);

			while (st.hasMoreTokens()) {
				ivec[i] = (byte) (Integer.parseInt(st.nextToken()) - 128);
				i++;
			}
		}
	}

	@Override
	public byte[] binaryHeader() {
		return "".getBytes();
	}

	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public ByteFV getFeatureVector() {
		return new ByteFV(ivec);
	}

	@Override
	public KeypointLocation getLocation() {
		return new KeypointLocation(x, y, ori, scale);
	}

	/**
	 * Set the location of this {@link Keypoint}
	 *
	 * @param location
	 *            the location
	 */
	public void setLocation(KeypointLocation location) {
		x = location.x;
		y = location.y;
		scale = location.scale;
		ori = location.orientation;
	}

	/**
	 * Create a list of {@link Keypoint}s from the input list, but with the
	 * positions offset by the given amount.
	 *
	 * @param keypoints
	 *            the input list
	 * @param x
	 *            the x offset
	 * @param y
	 *            the y offset
	 * @return the new keypoints
	 */
	public static List<Keypoint> getRelativeKeypoints(List<Keypoint> keypoints, float x, float y) {
		final List<Keypoint> shifted = new ArrayList<Keypoint>();
		for (final Keypoint old : keypoints) {
			final Keypoint n = new Keypoint();
			n.x = old.x - x;
			n.y = old.y - y;
			n.ivec = old.ivec;
			n.scale = old.scale;
			n.ori = old.ori;
			shifted.add(n);
		}
		return shifted;
	}

	/**
	 * Add Gaussian noise the feature vectors of some features. The original
	 * features are untouched; the returned list contains a copy.
	 *
	 * @param siftFeatures
	 *            the input features
	 * @param mean
	 *            the mean of the noise
	 * @param sigma
	 *            the standard deviation of the noise
	 * @return the noisy keypoints
	 */
	public static List<Keypoint> addGaussianNoise(List<Keypoint> siftFeatures, double mean, double sigma) {
		final List<Keypoint> toRet = new ArrayList<Keypoint>();
		for (final Keypoint keypoint : siftFeatures) {
			final Keypoint kpClone = keypoint.clone();
			for (int i = 0; i < keypoint.ivec.length; i++) {
				final double deviation = Normal.staticNextDouble(mean, sigma);
				int value = 0xff & keypoint.ivec[i];
				value += deviation;
				if (value < 0)
					value = 0;
				else if (value > 255)
					value = 255;

				kpClone.ivec[i] = (byte) value;
			}
			toRet.add(kpClone);
		}
		return toRet;
	}

	/**
	 * Scale a list of keypoints by the given amount. This scales the location
	 * and scale of each keypoint. The original features are untouched; the
	 * returned list contains a copy.
	 *
	 * @param keypoints
	 *            the input features.
	 * @param toScale
	 *            the scale factor
	 * @return the scaled features.
	 */
	public static List<Keypoint> getScaledKeypoints(List<Keypoint> keypoints, int toScale) {
		final List<Keypoint> shifted = new ArrayList<Keypoint>();
		for (final Keypoint old : keypoints) {
			final Keypoint n = new Keypoint();
			n.x = old.x * toScale;
			n.y = old.y * toScale;
			n.ivec = old.ivec;
			n.scale = old.scale * toScale;
			n.ori = old.ori;
			shifted.add(n);
		}
		return shifted;
	}

	@Override
	public void translate(float x, float y) {
		this.x += x;
		this.y += y;
	}

	@Override
	public Keypoint transform(Matrix transform) {
		float xt = (float) transform.get(0, 0) * getX() + (float) transform.get(0, 1) * getY()
				+ (float) transform.get(0, 2);
		float yt = (float) transform.get(1, 0) * getX() + (float) transform.get(1, 1) * getY()
				+ (float) transform.get(1, 2);
		final float zt = (float) transform.get(2, 0) * getX() + (float) transform.get(2, 1) * getY()
				+ (float) transform.get(2, 2);

		xt /= zt;
		yt /= zt;

		return new Keypoint(xt, yt, this.ori, this.scale, this.ivec.clone());
	}

	@Override
	public Point2d minus(Point2d a) {
		final Keypoint kp = this.clone();
		kp.x = this.x - (int) a.getX();
		kp.y = this.y - (int) a.getY();
		return null;
	}

	@Override
	public void translate(Point2d v) {
		this.translate(v.getX(), v.getY());
	}

	@Override
	public Point2d copy() {
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
