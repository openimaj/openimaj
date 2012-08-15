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
import org.openimaj.image.feature.FImage2FloatFV;
import org.openimaj.image.processing.face.alignment.FaceAligner;
import org.openimaj.image.processing.face.alignment.ScalingAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.io.IOUtils;

/**
 * A {@link FacialFeature} that is just the pixel values
 * of a (possibly aligned) face detection. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FaceImageFeature implements FacialFeature, FeatureVectorProvider<FloatFV> {
	/**
	 * A {@link FacialFeatureExtractor} for producing {@link FaceImageFeature}s.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 * @param <T> Type of {@link DetectedFace}
	 */
	public static class Extractor<T extends DetectedFace> implements FacialFeatureExtractor<FaceImageFeature, T> {
		FaceAligner<T> aligner;
		
		/**
		 * Construct with a {@link ScalingAligner} with its default resolution
		 */
		public Extractor() {
			this(new ScalingAligner<T>());
		}
		
		/**
		 * Construct with an aligner
		 * @param aligner the aligner
		 */
		public Extractor(FaceAligner<T> aligner) {
			this.aligner = aligner;
		}
		
		@Override
		public FaceImageFeature extractFeature(T face) {
			FImage faceImage = aligner.align(face);
			FloatFV feature = FImage2FloatFV.INSTANCE.extractFeature(faceImage);
			
			return new FaceImageFeature(feature);
		}

		@Override
		public void readBinary(DataInput in) throws IOException {
			String alignerClass = in.readUTF();
			aligner = IOUtils.newInstance(alignerClass);
			aligner.readBinary(in);
		}

		@Override
		public byte[] binaryHeader() {
			return this.getClass().getName().getBytes();
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			out.writeUTF(aligner.getClass().getName());
			aligner.writeBinary(out);
		}
	}

	private FloatFV feature;
	
	/**
	 * Construct with the given feature
	 * 
	 * @param feature
	 */
	public FaceImageFeature(FloatFV feature) {
		this.feature = feature;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		feature = new FloatFV();
		feature.readBinary(in);
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		feature.writeBinary(out);
	}

	@Override
	public FloatFV getFeatureVector() {
		return feature;
	}
}
