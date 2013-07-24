package org.openimaj.image.feature.dense.gradient.dsift;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.openimaj.feature.FloatFV;

/**
 * Dense SIFT keypoint with a location and float feature vector. Also includes
 * the energy of the feature prior to normalisation in case low-contrast
 * features need removing.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class FloatDSIFTKeypoint
		extends
		AbstractDSIFTKeypoint<FloatFV, float[]>
{
	static final long serialVersionUID = 12345545L;

	/**
	 * Construct with the default feature vector length for SIFT (128).
	 */
	public FloatDSIFTKeypoint() {
		this(DEFAULT_LENGTH);
	}

	/**
	 * Construct with the given feature vector length.
	 * 
	 * @param length
	 *            the length of the feature vector
	 */
	public FloatDSIFTKeypoint(int length) {
		this.descriptor = new float[length];
	}

	/**
	 * Construct with the given parameters.
	 * 
	 * @param x
	 *            the x-ordinate of the keypoint
	 * @param y
	 *            the y-ordinate of the keypoint
	 * @param descriptor
	 *            the feature vector of the keypoint
	 * @param energy
	 *            the energy of the keypoint
	 */
	public FloatDSIFTKeypoint(final float x, final float y, final float[] descriptor, final float energy) {
		this.x = x;
		this.y = y;
		this.descriptor = descriptor;
		this.energy = energy;
	}

	@Override
	public FloatFV getFeatureVector() {
		return new FloatFV(descriptor);
	}

	@Override
	public String toString() {
		return ("FloatDSIFTKeypoint(" + this.x + ", " + this.y + ")");
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeFloat(x);
		out.writeFloat(y);
		out.writeFloat(energy);

		for (final float f : descriptor)
			out.writeFloat(f);
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		/* Output data for the keypoint. */
		out.write(x + " " + y + " " + energy + "\n");

		for (int i = 0; i < descriptor.length; i++) {
			if (i > 0 && i % 20 == 0)
				out.println();
			out.print(" %f" + descriptor[i]);
		}
		out.println();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		x = in.readFloat();
		y = in.readFloat();
		energy = in.readFloat();
		for (int i = 0; i < descriptor.length; i++)
			descriptor[i] = in.readFloat();
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		x = in.nextFloat();
		y = in.nextFloat();
		energy = in.nextFloat();

		int i = 0;
		while (i < descriptor.length) {
			final String line = in.nextLine();
			final StringTokenizer st = new StringTokenizer(line);

			while (st.hasMoreTokens()) {
				descriptor[i] = Float.parseFloat(st.nextToken());
				i++;
			}
		}
	}
}
