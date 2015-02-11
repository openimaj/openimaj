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

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Scanner;

import org.openimaj.feature.ByteFV;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.feature.local.keypoints.SIFTGeoKeypoint.SIFTGeoLocation;
import org.openimaj.io.IOUtils;
import org.openimaj.io.VariableLength;

import Jama.Matrix;

/**
 * Implementation of a {@link LocalFeature} based on the .siftgeo format
 * developed by Krystian Mikolajczyk for his tools.
 * <p>
 * Because the .siftgeo file-format is custom, it isn't directly compatible with
 * that read by
 * {@link MemoryLocalFeatureList#read(java.io.BufferedInputStream, Class)} or
 * written with {@link IOUtils}. To work-around these issues, this class
 * implements a set of static I/O methods for reading and writing multiple
 * features to/from a standard .siftgeo file.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class SIFTGeoKeypoint implements LocalFeature<SIFTGeoLocation, ByteFV>, VariableLength, Cloneable, Serializable {
	/**
	 * The location of a {@link SIFTGeoKeypoint}.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	public class SIFTGeoLocation extends KeypointLocation {
		private static final long serialVersionUID = 1L;

		// number of bytes when written as binary
		private static final int NUM_BYTES = 36;

		/**
		 * The saliency of the interest point
		 */
		public float cornerness;

		/**
		 * affine parameters of the interest point
		 */
		public Matrix affine;

		/**
		 * Construct with the given parameters
		 *
		 * @param x
		 *            x-ordinate of feature
		 * @param y
		 *            y-ordinate of feature
		 * @param scale
		 *            scale of feature
		 * @param orientation
		 *            orientation of feature
		 * @param cornerness
		 *            the saliency of the interest point
		 * @param affine
		 *            affine parameters
		 */
		public SIFTGeoLocation(float x, float y, float orientation, float scale, float cornerness, Matrix affine) {
			super(x, y, scale, orientation);
			this.cornerness = cornerness;
			this.affine = affine;
		}

		/**
		 * Default constructor. Everything set to zero with the exception of the
		 * affine parameters which are set to the identity matrix.
		 */
		public SIFTGeoLocation() {
			affine = Matrix.identity(2, 2);
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			final ByteBuffer buffer = ByteBuffer.allocate(SIFTGeoLocation.NUM_BYTES);
			buffer.order(ByteOrder.LITTLE_ENDIAN);

			writeBinary(buffer);
		}

		private void writeBinary(ByteBuffer buffer) {
			buffer.putFloat(x);
			buffer.putFloat(y);
			buffer.putFloat(scale);
			buffer.putFloat(orientation);
			buffer.putFloat((float) affine.get(0, 0));
			buffer.putFloat((float) affine.get(0, 1));
			buffer.putFloat((float) affine.get(1, 0));
			buffer.putFloat((float) affine.get(1, 1));
			buffer.putFloat(cornerness);
		}

		@Override
		public void writeASCII(PrintWriter out) throws IOException {
			out.format("%4.2f %4.2f %4.2f %4.3f %4.3f %4.3f %4.3f %4.3f", x, y, scale, orientation,
					(float) affine.get(0, 0), (float) affine.get(0, 1), (float) affine
					.get(1, 0), (float) affine.get(1, 1));
			out.println();
		}

		@Override
		public void readBinary(DataInput in) throws IOException {
			final byte[] array = new byte[NUM_BYTES];
			in.readFully(array);

			final ByteBuffer buffer = ByteBuffer.wrap(array);
			buffer.order(ByteOrder.LITTLE_ENDIAN);

			readBinary(buffer);
		}

		private void readBinary(ByteBuffer buffer) {
			x = buffer.getFloat();
			y = buffer.getFloat();
			scale = buffer.getFloat();
			orientation = buffer.getFloat();
			affine.set(0, 0, buffer.getFloat());
			affine.set(0, 1, buffer.getFloat());
			affine.set(1, 0, buffer.getFloat());
			affine.set(1, 1, buffer.getFloat());
			cornerness = buffer.getFloat();
		}

		@Override
		public void readASCII(Scanner in) throws IOException {
			x = Float.parseFloat(in.next());
			y = Float.parseFloat(in.next());
			scale = Float.parseFloat(in.next());
			orientation = Float.parseFloat(in.next());
			affine.set(0, 0, Float.parseFloat(in.next()));
			affine.set(0, 1, Float.parseFloat(in.next()));
			affine.set(1, 0, Float.parseFloat(in.next()));
			affine.set(1, 1, Float.parseFloat(in.next()));
			cornerness = Float.parseFloat(in.next());
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
		public Float getOrdinate(int dimension) {
			final float[] pos = { x, y, scale, orientation, (float) affine.get(0, 0), (float) affine.get(0, 1),
					(float) affine
							.get(1, 0), (float) affine.get(1, 1) };
			return pos[dimension];
		}
	}

	private static final long serialVersionUID = 1L;

	/**
	 * The location of the point
	 */
	public SIFTGeoLocation location = new SIFTGeoLocation();

	/**
	 * The descriptor
	 */
	public byte[] descriptor;

	/**
	 * Construct with the location set to zero, and with an empty descriptor of
	 * the given length.
	 *
	 * @param len
	 *            the descriptor length
	 */
	public SIFTGeoKeypoint(int len) {
		descriptor = new byte[len];
	}

	/**
	 * Construct with the given parameters
	 *
	 * @param x
	 *            x-ordinate of feature
	 *
	 * @param y
	 *            y-ordinate of feature
	 *
	 * @param scale
	 *            scale of feature
	 *
	 * @param orientation
	 *            orientation of feature
	 *
	 * @param cornerness
	 *            the saliency of the interest point
	 *
	 * @param affine
	 *            affine parameters
	 * @param descriptor
	 *            the descriptor
	 */
	public SIFTGeoKeypoint(float x, float y, float orientation, float scale, float cornerness, Matrix affine,
			byte[] descriptor)
	{
		this.location.x = x;
		this.location.y = y;
		this.location.orientation = orientation;
		this.location.scale = scale;
		this.location.cornerness = cornerness;
		this.location.affine = affine;
		this.descriptor = descriptor;
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		location.readASCII(in);
		final int len = in.nextInt();

		descriptor = new byte[len];
		for (int i = 0; i < len; i++)
			descriptor[i] = (byte) (in.nextInt() - 128);
	}

	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		location.readBinary(in);

		final byte[] array = new byte[4];
		in.readFully(array);

		final ByteBuffer buffer = ByteBuffer.wrap(array);

		buffer.order(ByteOrder.LITTLE_ENDIAN);
		final int len = buffer.getInt();

		descriptor = new byte[len];
		for (int i = 0; i < descriptor.length; i++)
			descriptor[i] = (byte) (in.readUnsignedByte() - 128);
	}

	@Override
	public byte[] binaryHeader() {
		return new byte[0]; // legacy files are "headerless"
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		location.writeASCII(out);
		out.format("%d\n", descriptor.length);
		for (int i = 0; i < descriptor.length; i++)
			out.format("%d ", descriptor[i] + 128);
		out.append("\n");
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		final ByteBuffer buffer = ByteBuffer.allocate(SIFTGeoLocation.NUM_BYTES + 4 + descriptor.length);
		buffer.order(ByteOrder.LITTLE_ENDIAN);

		location.writeBinary(buffer);
		buffer.putInt(descriptor.length);

		for (int i = 0; i < descriptor.length; i++)
			buffer.put((byte) ((descriptor[i] + 128) & 0xFF));

		out.write(buffer.array());
	}

	@Override
	public ByteFV getFeatureVector() {
		return new ByteFV(descriptor);
	}

	@Override
	public SIFTGeoLocation getLocation() {
		return location;
	}

	@Override
	public String toString() {
		final StringWriter sw = new StringWriter();

		try {
			writeASCII(new PrintWriter(sw));
		} catch (final IOException e) {
		}

		return sw.toString();
	}

	/**
	 * Read a .siftgeo file.
	 *
	 * @param file
	 *            the file
	 * @return the list of read {@link SIFTGeoKeypoint}s
	 * @throws IOException
	 *             if an error occurs during reading
	 */
	public static LocalFeatureList<SIFTGeoKeypoint> read(File file) throws IOException {
		return read(new BufferedInputStream(new FileInputStream(file)));
	}

	/**
	 * Read .siftgeo file from a stream.
	 *
	 * @param stream
	 *            the stream
	 * @return the list of read {@link SIFTGeoKeypoint}s
	 * @throws IOException
	 *             if an error occurs during reading
	 */
	public static LocalFeatureList<SIFTGeoKeypoint> read(InputStream stream) throws IOException {
		return read(new DataInputStream(stream));
	}

	/**
	 * Read .siftgeo file from a stream.
	 *
	 * @param stream
	 *            the stream
	 * @return the list of read {@link SIFTGeoKeypoint}s
	 * @throws IOException
	 *             if an error occurs during reading
	 */
	public static LocalFeatureList<SIFTGeoKeypoint> read(DataInputStream stream) throws IOException {
		final MemoryLocalFeatureList<SIFTGeoKeypoint> keys = new MemoryLocalFeatureList<SIFTGeoKeypoint>();

		while (true) {
			try {
				final SIFTGeoKeypoint kp = new SIFTGeoKeypoint(0);
				kp.readBinary(stream);
				keys.add(kp);
			} catch (final EOFException eof) {
				// end of stream
				return keys;
			}
		}
	}

	/**
	 * Write a .siftgeo file
	 *
	 * @param keys
	 *            the {@link SIFTGeoKeypoint}s to write
	 * @param file
	 *            the file
	 * @throws IOException
	 *             if an error occurs whilst writing
	 */
	public static void write(List<SIFTGeoKeypoint> keys, File file) throws IOException {
		write(keys, new FileOutputStream(file));
	}

	/**
	 * Write a .siftgeo stream
	 *
	 * @param keys
	 *            the {@link SIFTGeoKeypoint}s to write
	 * @param stream
	 *            the stream
	 * @throws IOException
	 *             if an error occurs whilst writing
	 */
	public static void write(List<SIFTGeoKeypoint> keys, OutputStream stream) throws IOException {
		write(keys, new DataOutputStream(stream));
	}

	/**
	 * Write a .siftgeo stream
	 *
	 * @param keys
	 *            the {@link SIFTGeoKeypoint}s to write
	 * @param stream
	 *            the stream
	 * @throws IOException
	 *             if an error occurs whilst writing
	 */
	public static void write(List<SIFTGeoKeypoint> keys, DataOutputStream stream) throws IOException {
		for (final SIFTGeoKeypoint k : keys) {
			k.writeBinary(stream);
		}
	}
}
