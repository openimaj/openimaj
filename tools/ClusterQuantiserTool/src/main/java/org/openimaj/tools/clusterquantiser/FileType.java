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
package org.openimaj.tools.clusterquantiser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Scanner;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.feature.local.affine.AffineSimulationKeypoint;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.io.IOUtils;

/**
 * Different file formats containing local features.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public enum FileType {
	/**
	 * Auto-guess between Lowe's ASCII keypoints format or the OpenIMAJ binary
	 * format.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	LOWE_KEYPOINT {
		@Override
		public Header readHeader(File file) throws IOException {
			try {
				return BINARY_KEYPOINT.readHeader(file);
			} catch (final Exception e) {
				return LOWE_KEYPOINT_ASCII.readHeader(file);
			}
		}

		@Override
		public Header readHeader(InputStream bis) throws IOException {
			final BufferedInputStream bstream = new BufferedInputStream(bis);

			final boolean binary = IOUtils.isBinary(bstream, LocalFeatureList.BINARY_HEADER);

			if (binary)
				return BINARY_KEYPOINT.readHeader(bstream);
			else
				return LOWE_KEYPOINT_ASCII.readHeader(bstream);
		}

		@Override
		public FeatureFile read(File file) throws IOException {
			try {
				return BINARY_KEYPOINT.read(file);
			} catch (final Exception e) {
				return LOWE_KEYPOINT_ASCII.read(file);
			}
		}

		@Override
		public FeatureFile read(InputStream stream) throws IOException {
			final BufferedInputStream bstream = new BufferedInputStream(stream);

			final boolean binary = IOUtils.isBinary(bstream, LocalFeatureList.BINARY_HEADER);

			if (binary)
				return BINARY_KEYPOINT.read(bstream);
			else
				return LOWE_KEYPOINT_ASCII.read(bstream);
		}

		@Override
		public byte[][] readFeatures(File file, int... index) throws IOException {
			try {
				return BINARY_KEYPOINT.readFeatures(file, index);
			} catch (final Exception e) {
				return LOWE_KEYPOINT_ASCII.readFeatures(file, index);
			}
		}

		@Override
		public byte[][] readFeatures(InputStream file, int... index) throws IOException {
			try {
				return BINARY_KEYPOINT.readFeatures(file, index);
			} catch (final Exception e) {
				return LOWE_KEYPOINT_ASCII.readFeatures(file, index);
			}
		}
	},
	/**
	 * OpenIMAJ binary list of keypoints format
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	BINARY_KEYPOINT {
		@Override
		public Header readHeader(File file) throws IOException {
			BufferedInputStream bis = null;

			try {
				bis = new BufferedInputStream(new FileInputStream(file));
				final byte[] header = new byte[LocalFeatureList.BINARY_HEADER.length];
				bis.read(header, 0, LocalFeatureList.BINARY_HEADER.length);

				if (!Arrays.equals(header, LocalFeatureList.BINARY_HEADER)) {
					throw new IOException("File \"" + file + "\"is not a binary keypoint file");
				}

				final DataInputStream dis = new DataInputStream(bis);

				final Header h = new Header();
				h.nfeatures = dis.readInt();
				h.ndims = dis.readInt();
				return h;
			} finally {
				try {
					bis.close();
				} catch (final IOException e) {
				}
			}
		}

		@Override
		public Header readHeader(InputStream bis) throws IOException {
			final byte[] header = new byte[LocalFeatureList.BINARY_HEADER.length];
			bis.read(header, 0, LocalFeatureList.BINARY_HEADER.length);

			if (!Arrays.equals(header, LocalFeatureList.BINARY_HEADER)) {
				throw new IOException("Stream does not contain a binary keypoint");
			}

			final DataInputStream dis = new DataInputStream(bis);

			final Header h = new Header();
			h.nfeatures = dis.readInt();
			h.ndims = dis.readInt();
			return h;
		}

		@Override
		public FeatureFile read(File file) throws IOException {
			final FeatureFile ff = new StreamedFeatureFile(file);
			return ff;
		}

		@Override
		public FeatureFile read(InputStream stream) throws IOException {
			final FeatureFile ff = new StreamedFeatureFile(stream);
			return ff;
		}

		@Override
		public byte[][] readFeatures(File file, int... index) throws IOException {
			return readFeatures(new FileInputStream(file), index);
		}

		@Override
		public byte[][] readFeatures(InputStream file, int... index) throws IOException {
			BufferedInputStream bis = null;
			final byte[][] data = new byte[index.length][];
			try {
				bis = new BufferedInputStream(file);
				final byte[] header = new byte[LocalFeatureList.BINARY_HEADER.length];
				bis.read(header, 0, LocalFeatureList.BINARY_HEADER.length);

				if (!Arrays.equals(header, LocalFeatureList.BINARY_HEADER)) {
					throw new IOException("File \"" + file + "\"is not a binary keypoint file");
				}

				final DataInputStream dis = new DataInputStream(bis);

				final Header h = new Header();
				h.nfeatures = dis.readInt();
				h.ndims = dis.readInt();

				// == float * 4 + int * KeypointEngine.VecLength
				final int vecLength = (16 + h.ndims);
				int skipped = 0;
				Arrays.sort(index);
				for (int i = 0; i < index.length; i++) {
					int toSkip = (index[i] * vecLength) - skipped;
					skipped += toSkip;
					while (toSkip > 0)
						toSkip -= dis.skip(toSkip);

					final Keypoint kp = new Keypoint();
					kp.x = dis.readFloat();
					kp.y = dis.readFloat();
					kp.scale = dis.readFloat();
					kp.ori = dis.readFloat();
					kp.ivec = new byte[h.ndims];
					dis.read(kp.ivec, 0, h.ndims);
					data[i] = kp.ivec;
					skipped += vecLength;
				}

			} finally {
				try {
					bis.close();
				} catch (final IOException e) {
				}
			}
			return data;
		}
	},
	/**
	 * Format defined by Lowe's "keypoints" binary
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	LOWE_KEYPOINT_ASCII {
		@Override
		public byte[][] readFeatures(File file, int... index) throws IOException {
			return AsciiInterestPoint.readData(file, index, false, AsciiInterestPoint.NUM_CIRCLE_LOC_FEATS);
		}

		@Override
		public Header readHeader(File file) throws IOException {
			return AsciiInterestPoint.readHeader(file, false);
		}

		@Override
		public Header readHeader(InputStream stream) throws IOException {
			return AsciiInterestPoint.readHeader(new Scanner(stream), false);
		}

		@Override
		public byte[][] readFeatures(File file) throws IOException {
			return AsciiInterestPoint.readData(file, false, AsciiInterestPoint.NUM_CIRCLE_LOC_FEATS);
		}

		@Override
		public FeatureFile read(File file) throws IOException {
			return AsciiInterestPoint.read(file, false, AsciiInterestPoint.NUM_CIRCLE_LOC_FEATS);
		}

		@Override
		public FeatureFile read(InputStream source) throws IOException {
			return AsciiInterestPoint.read(source, false, AsciiInterestPoint.NUM_CIRCLE_LOC_FEATS);
		}
	},
	/**
	 * Ellipse format used by Oxford tools
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	ELLIPSE_ASCII {
		@Override
		public byte[][] readFeatures(File file, int... index) throws IOException {
			return AsciiInterestPoint.readData(file, index, true, AsciiInterestPoint.NUM_ELLIPSE_LOC_FEATS);
		}

		@Override
		public Header readHeader(File file) throws IOException {
			return AsciiInterestPoint.readHeader(file, true);
		}

		@Override
		public Header readHeader(InputStream stream) throws IOException {
			return AsciiInterestPoint.readHeader(new Scanner(stream), true);
		}

		@Override
		public byte[][] readFeatures(File file) throws IOException {
			return AsciiInterestPoint.readData(file, true, AsciiInterestPoint.NUM_ELLIPSE_LOC_FEATS);
		}

		@Override
		public FeatureFile read(File file) throws IOException {
			return AsciiInterestPoint.read(file, true, AsciiInterestPoint.NUM_ELLIPSE_LOC_FEATS);
		}

		@Override
		public FeatureFile read(InputStream source) throws IOException {
			return AsciiInterestPoint.read(source, true, AsciiInterestPoint.NUM_ELLIPSE_LOC_FEATS);
		}
	},
	/**
	 * KOEN1 ascii format used by Koen van der Sande's colour sift tools.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	KOEN1_ASCII {
		@Override
		public FeatureFile read(InputStream file) throws IOException {
			// create a BufferedReader for the file
			final BufferedReader input = new BufferedReader(new InputStreamReader(file));

			// read the first line and check that it starts with KOEN1
			// this way there is no need to worry about newline characters
			// in the end of string, if we used .equals
			if (!(input.readLine().startsWith("KOEN1"))) {
				throw new IOException(
						"The specified file is not a KOEN1 type file");
			} else {
				// read the next two lines and Integer.parseInt(); to get ndims
				// & nfeatures
				final int ndims = Integer.parseInt(input.readLine());
				final int nfeatures = Integer.parseInt(input.readLine());

				final byte[][] data = new byte[nfeatures][ndims];
				final String[] locations = new String[nfeatures];

				if (nfeatures == 0) {
					final FeatureFile ff = new MemoryFeatureFile(new byte[0][], new String[0]);
					return ff;
				}

				for (int i = 0; i < nfeatures; i++) {

					// read the next line and split on ';'
					final String[] parts = input.readLine().split(";");

					// put first element (substring) of the split into
					// FeatureFile.locationInfo
					locations[i] = parts[0];

					// split second element (substring) on ' ' (a space)
					final String[] fvector = parts[1].trim().split(" ");
					// parse each element as int and put into array
					for (int j = 0; j < ndims; j++) {
						// store array in FeatureFiel.data
						data[i][j] = (byte) (Integer.parseInt(fvector[j]) - 128);
					}

				}
				final FeatureFile ff = new MemoryFeatureFile(data, locations);
				// return FeatureFile
				return ff;
			}
		}

		@Override
		public FeatureFile read(File source) throws IOException {
			return read(new FileInputStream(source));
		}
	},
	/**
	 * OpenIMAJ ASIFTENRICHED format
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	ASIFTENRICHED {
		@Override
		public Header readHeader(File file) throws IOException {

			try {
				return ASIFTENRICHED_BINARY.readHeader(file);
			}
			catch (final Exception e) {
				return ASIFTENRICHED_ASCII.readHeader(file);
			}
		}

		@Override
		public Header readHeader(InputStream bis) throws IOException {
			final BufferedInputStream bstream = new BufferedInputStream(bis);

			final boolean binary = IOUtils.isBinary(bstream, LocalFeatureList.BINARY_HEADER);

			if (binary)
				return ASIFTENRICHED_BINARY.readHeader(bstream);
			else
				return ASIFTENRICHED_ASCII.readHeader(bstream);

		}

		@Override
		public FeatureFile read(File file) throws IOException {
			try {
				return ASIFTENRICHED_BINARY.read(file);
			}
			catch (final Exception e) {
				return ASIFTENRICHED_ASCII.read(file);
			}
		}

		@Override
		public FeatureFile read(InputStream stream) throws IOException {
			final BufferedInputStream bstream = new BufferedInputStream(stream);

			final boolean binary = IOUtils.isBinary(bstream, LocalFeatureList.BINARY_HEADER);

			if (binary)
				return ASIFTENRICHED_BINARY.read(bstream);
			else
				return ASIFTENRICHED_ASCII.read(bstream);
		}

		@Override
		public byte[][] readFeatures(File file, int... index) throws IOException {
			try {
				return ASIFTENRICHED_BINARY.readFeatures(file, index);
			}
			catch (final Exception e) {
				return ASIFTENRICHED_ASCII.readFeatures(file, index);
			}
		}

		@Override
		public byte[][] readFeatures(InputStream file, int... index) throws IOException {
			try {
				return ASIFTENRICHED_BINARY.readFeatures(file, index);
			}
			catch (final Exception e) {
				return ASIFTENRICHED_ASCII.readFeatures(file, index);
			}
		}
	},
	/**
	 * OpenIMAJ ASIFTENRICHED binary format
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	ASIFTENRICHED_BINARY {
		@Override
		public Header readHeader(File file) throws IOException {
			BufferedInputStream bis = null;

			try {
				bis = new BufferedInputStream(new FileInputStream(file));
				final byte[] header = new byte[LocalFeatureList.BINARY_HEADER.length];
				bis.read(header, 0, LocalFeatureList.BINARY_HEADER.length);

				if (!Arrays.equals(header, LocalFeatureList.BINARY_HEADER)) {
					throw new IOException("File \"" + file + "\"is not a binary keypoint file");
				}

				final DataInputStream dis = new DataInputStream(bis);

				final Header h = new Header();
				h.nfeatures = dis.readInt();
				h.ndims = dis.readInt();
				return h;
			} finally {
				try {
					bis.close();
				} catch (final IOException e) {
				}
			}
		}

		@Override
		public Header readHeader(InputStream bis) throws IOException {
			final byte[] header = new byte[LocalFeatureList.BINARY_HEADER.length];
			bis.read(header, 0, LocalFeatureList.BINARY_HEADER.length);

			if (!Arrays.equals(header, LocalFeatureList.BINARY_HEADER)) {
				throw new IOException("Strean dies not contain a binary keypoint");
			}

			final DataInputStream dis = new DataInputStream(bis);

			final Header h = new Header();
			h.nfeatures = dis.readInt();
			h.ndims = dis.readInt();
			return h;
		}

		@Override
		public FeatureFile read(File file) throws IOException {
			final StreamedFeatureFile ff = new StreamedFeatureFile(file, AffineSimulationKeypoint.class);
			ff.setIteratorType(AffineSimulationKeypointListArrayIterator.class);
			return ff;
		}

		@Override
		public FeatureFile read(InputStream stream) throws IOException {
			final StreamedFeatureFile ff = new StreamedFeatureFile(stream, AffineSimulationKeypoint.class);
			ff.setIteratorType(AffineSimulationKeypointListArrayIterator.class);
			return ff;
		}

		@Override
		public byte[][] readFeatures(File file, int... index) throws IOException {
			return readFeatures(new FileInputStream(file), index);
		}

		@Override
		public byte[][] readFeatures(InputStream file, int... index) throws IOException {
			BufferedInputStream bis = null;
			final byte[][] data = new byte[index.length][];
			try {
				bis = new BufferedInputStream(file);
				final byte[] header = new byte[LocalFeatureList.BINARY_HEADER.length];
				bis.read(header, 0, LocalFeatureList.BINARY_HEADER.length);

				if (!Arrays.equals(header, LocalFeatureList.BINARY_HEADER)) {
					throw new IOException("File \"" + file + "\"is not a binary keypoint file");
				}

				final DataInputStream dis = new DataInputStream(bis);

				final Header h = new Header();
				h.nfeatures = dis.readInt();
				h.ndims = dis.readInt();

				// == float * 6 + int + KeypointEngine.VecLength
				final int vecLength = (28 + h.ndims);
				int skipped = 0;
				Arrays.sort(index);
				for (int i = 0; i < index.length; i++) {
					int toSkip = (index[i] * vecLength) - skipped;
					skipped += toSkip;
					while (toSkip > 0)
						toSkip -= dis.skip(toSkip);

					final AffineSimulationKeypoint kp = new AffineSimulationKeypoint(h.ndims);
					kp.readBinary(dis);
					data[i] = kp.ivec;
					skipped += vecLength;
				}

			} finally {
				try {
					bis.close();
				} catch (final IOException e) {
				}
			}
			return data;
		}
	},
	/**
	 * OpenIMAJ ASIFTENRICHED ascii format
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	ASIFTENRICHED_ASCII {
		@Override
		public byte[][] readFeatures(File file, int... index) throws IOException {
			return AsciiInterestPoint.readData(file, index, false, AsciiInterestPoint.NUM_ASIFT_LOC_FEATS);
		}

		@Override
		public Header readHeader(File file) throws IOException {
			return AsciiInterestPoint.readHeader(file, false);
		}

		@Override
		public Header readHeader(InputStream stream) throws IOException {
			return AsciiInterestPoint.readHeader(new Scanner(stream), false);
		}

		@Override
		public byte[][] readFeatures(File file) throws IOException {
			return AsciiInterestPoint.readData(file, false, AsciiInterestPoint.NUM_ASIFT_LOC_FEATS);
		}

		@Override
		public FeatureFile read(File file) throws IOException {
			return AsciiInterestPoint.read(file, false, AsciiInterestPoint.NUM_ASIFT_LOC_FEATS);
		}

		@Override
		public FeatureFile read(InputStream source) throws IOException {
			return AsciiInterestPoint.read(source, false, AsciiInterestPoint.NUM_ASIFT_LOC_FEATS);
		}
	};

	/**
	 * Read the header (num features and dimensionality of features) from given
	 * file. Override for performance.
	 * 
	 * @param file
	 * @return header
	 * @throws IOException
	 */
	public Header readHeader(File file) throws IOException {
		final Header header = new Header();

		final FeatureFile ff = read(file);
		if (ff.size() > 0) {
			header.nfeatures = ff.size();
			header.ndims = ff.get(0).data.length;
		} else {
			header.nfeatures = 0;
			header.ndims = 0;
		}

		return header;
	}

	/**
	 * Read the header (num features and dimensionality of features) from given
	 * file. Override for performance.
	 * 
	 * @param stream
	 * @return header
	 * @throws IOException
	 */
	public Header readHeader(InputStream stream) throws IOException {
		final Header header = new Header();

		final FeatureFile ff = read(stream);
		if (ff.size() > 0) {
			header.nfeatures = ff.size();
			header.ndims = ff.get(0).data.length;
		} else {
			header.nfeatures = 0;
			header.ndims = 0;
		}

		return header;
	}

	/**
	 * Read features at given indices from the file. Override for performance.
	 * 
	 * @param file
	 * @param index
	 * @return the feature data
	 * @throws IOException
	 */
	public byte[][] readFeatures(File file, int... index) throws IOException {

		return readFeatures(new FileInputStream(file), index);
	}

	/**
	 * Read features at given indices from an input stream. Override for
	 * performance.
	 * 
	 * @param stream
	 * @param index
	 * @return the feature data
	 * @throws IOException
	 */
	public byte[][] readFeatures(InputStream stream, int... index) throws IOException {

		final byte[][] features = readFeatures(stream);
		final byte[][] selected = new byte[index.length][];
		for (int i = 0; i < index.length; i++) {
			selected[i] = features[index[i]];
		}
		return selected;
	}

	/**
	 * Read all the features from the file. Override for performance.
	 * 
	 * @param file
	 * @return the feature data
	 * @throws IOException
	 */
	public byte[][] readFeatures(File file) throws IOException {
		final FeatureFile ff = read(file);
		final byte[][] data = new byte[ff.size()][];
		int i = 0;
		for (final FeatureFileFeature fff : ff) {
			data[i++] = fff.data;
		}
		return data;
	}

	/**
	 * Read all the features from the file. Override for performance.
	 * 
	 * @param stream
	 * @return the feature data
	 * @throws IOException
	 */
	public byte[][] readFeatures(InputStream stream) throws IOException {
		final FeatureFile ff = read(stream);
		final byte[][] data = new byte[ff.size()][];
		int i = 0;
		for (final FeatureFileFeature fff : ff) {
			data[i++] = fff.data;
		}
		return data;
	}

	/**
	 * Read a file
	 * 
	 * @param file
	 * @return the features
	 * @throws IOException
	 */
	public abstract FeatureFile read(File file) throws IOException;

	/**
	 * Read a file
	 * 
	 * @param source
	 * @return the features
	 * @throws IOException
	 */
	public abstract FeatureFile read(InputStream source) throws IOException;
}
