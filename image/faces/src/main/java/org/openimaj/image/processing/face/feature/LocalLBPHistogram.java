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
package org.openimaj.image.processing.face.feature;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.dense.binarypattern.ExtendedLocalBinaryPattern;
import org.openimaj.image.feature.dense.binarypattern.UniformBinaryPattern;
import org.openimaj.image.processing.face.alignment.FaceAligner;
import org.openimaj.image.processing.face.alignment.IdentityAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.io.IOUtils;

/**
 * A {@link FacialFeature} built from decomposing the face image into
 * (non-overlapping) blocks and building histograms of the
 * {@link ExtendedLocalBinaryPattern}s for each block and then concatenating to
 * form the final feature.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class LocalLBPHistogram implements FacialFeature, FeatureVectorProvider<FloatFV> {
	/**
	 * A {@link FacialFeatureExtractor} for building {@link LocalLBPHistogram}s.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 * @param <T>
	 *            Type of {@link DetectedFace}.
	 */
	public static class Extractor<T extends DetectedFace> implements FacialFeatureExtractor<LocalLBPHistogram, T> {
		FaceAligner<T> aligner;
		int blocksX = 25;
		int blocksY = 25;
		int samples = 8;
		int radius = 1;

		/**
		 * Construct with a {@link IdentityAligner}
		 */
		public Extractor() {
			this.aligner = new IdentityAligner<T>();
		}

		/**
		 * Construct with the given aligner.
		 * 
		 * @param aligner
		 *            the aligner
		 */
		public Extractor(FaceAligner<T> aligner) {
			this.aligner = aligner;
		}

		/**
		 * Construct with the given aligner, parameters describing how the image
		 * is broken into blocks and parameters describing the radius of the LBP
		 * extraction circle, and how many samples are made.
		 * 
		 * @param aligner
		 *            The face aligner
		 * @param blocksX
		 *            The number of blocks in the x-direction
		 * @param blocksY
		 *            The number of blocks in the y-direction
		 * @param samples
		 *            The number of samples around the circle for the
		 *            {@link ExtendedLocalBinaryPattern}
		 * @param radius
		 *            the radius used for the {@link ExtendedLocalBinaryPattern}
		 *            .
		 */
		public Extractor(FaceAligner<T> aligner, int blocksX, int blocksY, int samples, int radius) {
			this.aligner = aligner;
			this.blocksX = blocksX;
			this.blocksY = blocksY;
			this.samples = samples;
			this.radius = radius;
		}

		@Override
		public LocalLBPHistogram extractFeature(T detectedFace) {
			final LocalLBPHistogram f = new LocalLBPHistogram();

			final FImage face = aligner.align(detectedFace);
			final FImage mask = aligner.getMask();

			f.initialise(face, mask, blocksX, blocksY, samples, radius);

			return f;
		}

		@Override
		public void readBinary(DataInput in) throws IOException {
			final String alignerClass = in.readUTF();
			aligner = IOUtils.newInstance(alignerClass);
			aligner.readBinary(in);

			blocksX = in.readInt();
			blocksY = in.readInt();
			radius = in.readInt();
			samples = in.readInt();
		}

		@Override
		public byte[] binaryHeader() {
			return this.getClass().getName().getBytes();
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			out.writeUTF(aligner.getClass().getName());
			aligner.writeBinary(out);

			out.writeInt(blocksX);
			out.writeInt(blocksY);
			out.writeInt(radius);
			out.writeInt(samples);
		}

		@Override
		public String toString() {
			return String.format("LocalLBPHistogram.Factory[blocksX=%d,blocksY=%d,samples=%d,radius=%d]", blocksX,
					blocksY, samples, radius);
		}
	}

	float[][][] histograms;
	transient FloatFV featureVector;

	protected void initialise(FImage face, FImage mask, int blocksX, int blocksY, int samples, int radius) {
		final int[][] pattern = ExtendedLocalBinaryPattern.calculateLBP(face, radius, samples);
		final boolean[][][] maps = UniformBinaryPattern.extractPatternMaps(pattern, samples);

		final int bx = face.width / blocksX;
		final int by = face.height / blocksY;
		histograms = new float[blocksY][blocksX][maps.length];

		// build histogram
		for (int p = 0; p < maps.length; p++) {
			for (int y = 0; y < blocksY; y++) {
				for (int x = 0; x < blocksX; x++) {

					for (int j = 0; j < by; j++) {
						for (int i = 0; i < bx; i++) {
							if (maps[p][y * by + j][x * bx + i])
								histograms[y][x][p]++;
						}
					}
				}
			}
		}

		// normalise
		for (int y = 0; y < blocksY; y++) {
			for (int x = 0; x < blocksX; x++) {
				float count = 0;
				for (int p = 0; p < maps.length; p++) {
					count += histograms[y][x][p];
				}
				for (int p = 0; p < maps.length; p++) {
					histograms[y][x][p] /= count;
				}
			}
		}

		updateFeatureVector();
	}

	protected void updateFeatureVector() {
		featureVector = new FloatFV(histograms.length * histograms[0].length * histograms[0][0].length);

		int i = 0;
		for (int y = 0; y < histograms.length; y++) {
			for (int x = 0; x < histograms[0].length; x++) {
				for (int p = 0; p < histograms[0][0].length; p++) {
					featureVector.values[i] = histograms[y][x][p];
					i++;
				}
			}
		}
	}

	@Override
	public byte[] binaryHeader() {
		return "LBPH".getBytes();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		final int by = in.readInt();
		final int bx = in.readInt();
		final int p = in.readInt();

		histograms = new float[by][bx][p];

		for (int j = 0; j < by; j++) {
			for (int i = 0; i < bx; i++) {
				for (int k = 0; k < p; k++) {
					histograms[j][i][k] = in.readFloat();
				}
			}
		}
		updateFeatureVector();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(histograms.length);
		out.writeInt(histograms[0].length);
		out.writeInt(histograms[0][0].length);

		for (final float[][] hist1 : histograms) {
			for (final float[] hist2 : hist1) {
				for (final float h : hist2) {
					out.writeFloat(h);
				}
			}
		}
	}

	@Override
	public FloatFV getFeatureVector() {
		if (featureVector == null)
			updateFeatureVector();

		return featureVector;
	}
}
