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

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.processing.face.detection.CLMDetectedFace;

/**
 * A feature-vector that describes the pose and shape of a face. Pose is modeled
 * as 3D pitch, yaw and roll. Shape is modeled as a list of eigen-weights on a
 * point distribution model.
 * <p>
 * This feature is potentially useful for classifying expressions.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class CLMPoseShapeFeature implements FacialFeature, FeatureVectorProvider<DoubleFV> {
	/**
	 * A {@link FacialFeatureExtractor} for providing
	 * {@link CLMPoseShapeFeature}s.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static class Extractor implements FacialFeatureExtractor<CLMPoseShapeFeature, CLMDetectedFace> {
		@Override
		public CLMPoseShapeFeature extractFeature(CLMDetectedFace face) {
			return new CLMPoseShapeFeature(face.getPoseShapeParameters());
		}

		@Override
		public void readBinary(DataInput in) throws IOException {
			// nothing to do
		}

		@Override
		public byte[] binaryHeader() {
			return this.getClass().getName().getBytes();
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			// nothing to do
		}
	}

	private DoubleFV fv;

	protected CLMPoseShapeFeature() {
		this(null);
	}

	/**
	 * Construct the {@link CLMPoseShapeFeature} with the given feature vector.
	 * 
	 * @param fv
	 *            the feature vector
	 */
	public CLMPoseShapeFeature(DoubleFV fv) {
		this.fv = fv;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		fv = new DoubleFV();
		fv.readBinary(in);
	}

	@Override
	public byte[] binaryHeader() {
		return getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		fv.writeBinary(out);
	}

	@Override
	public DoubleFV getFeatureVector() {
		return fv;
	}
}
