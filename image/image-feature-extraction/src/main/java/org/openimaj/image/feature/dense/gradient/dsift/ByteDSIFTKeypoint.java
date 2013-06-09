package org.openimaj.image.feature.dense.gradient.dsift;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.openimaj.feature.ByteFV;

/**
 * Dense SIFT keypoint with a location and byte feature vector. Also includes
 * the energy of the feature prior to normalisation in case low-contrast
 * features need removing.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class ByteDSIFTKeypoint
		extends
		AbstractDSIFTKeypoint<ByteFV, byte[]>
{
	static final long serialVersionUID = 12345545L;

	/**
	 * Construct with the default feature vector length for SIFT (128).
	 */
	public ByteDSIFTKeypoint() {
		this(DEFAULT_LENGTH);
	}

	/**
	 * Construct with the given feature vector length.
	 * 
	 * @param length
	 *            the length of the feature vector
	 */
	public ByteDSIFTKeypoint(final int length) {
		this.descriptor = new byte[length];
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
	public ByteDSIFTKeypoint(final float x, final float y, final byte[] descriptor, final float energy) {
		this.x = x;
		this.y = y;
		this.descriptor = descriptor;
		this.energy = energy;
	}

	/**
	 * Construct with the given parameters. The float version of the descriptor
	 * is converted to bytes by multiplying each bin by 512, clipping to 255 and
	 * then subtracting 128.
	 * 
	 * @param x
	 *            the x-ordinate of the keypoint
	 * @param y
	 *            the y-ordinate of the keypoint
	 * @param fdescriptor
	 *            the flaot version of feature vector of the keypoint
	 * @param energy
	 *            the energy of the keypoint
	 */
	public ByteDSIFTKeypoint(final float x, final float y, final float[] fdescriptor, final float energy) {
		this.x = x;
		this.y = y;
		this.energy = energy;
		this.descriptor = new byte[fdescriptor.length];

		for (int i = 0; i < descriptor.length; i++) {
			final int intval = (int) (512.0 * fdescriptor[i]);
			descriptor[i] = (byte) (Math.min(255, intval) - 128);
		}
	}

	@Override
	public ByteFV getFeatureVector() {
		return new ByteFV(descriptor);
	}

	@Override
	public String toString() {
		return ("ByteDSIFTKeypoint(" + this.x + ", " + this.y + ")");
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		getLocation().writeBinary(out);
		out.write(this.descriptor);
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		/* Output data for the keypoint. */
		getLocation().writeASCII(out);
		for (int i = 0; i < descriptor.length; i++) {
			if (i > 0 && i % 20 == 0)
				out.println();
			out.print(" " + (descriptor[i] + 128));
		}
		out.println();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		((DSIFTKeypointLocation) this).readBinary(in);
		in.readFully(descriptor);
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		((DSIFTKeypointLocation) this).readASCII(in);

		int i = 0;
		while (i < descriptor.length) {
			final String line = in.nextLine();
			final StringTokenizer st = new StringTokenizer(line);

			while (st.hasMoreTokens()) {
				descriptor[i] = (byte) (Integer.parseInt(st.nextToken()) - 128);
				i++;
			}
		}
	}
}
