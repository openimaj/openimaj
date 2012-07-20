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

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * A {@link FacialFeature} that uses DoG-SIFT features to 
 * describe a face.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class DoGSIFTFeature implements FacialFeature {
	/**
	 * A {@link FacialFeatureExtractor} for producing {@link DoGSIFTFeature}s
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	public static class Extractor implements FacialFeatureExtractor<DoGSIFTFeature, DetectedFace> {
		/**
		 * Default constructor
		 */
		public Extractor() {
			
		}
		
		@Override
		public void readBinary(DataInput in) throws IOException {
			// currently no state to write
		}

		@Override
		public byte[] binaryHeader() {
			return this.getClass().getName().getBytes();
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			// currently no state to read
		}

		@Override
		public DoGSIFTFeature extractFeature(DetectedFace face) {
			DoGSIFTFeature feature = new DoGSIFTFeature();
			feature.initialise(face);
			return feature;
		}
	}
	
	protected LocalFeatureList<Keypoint> keys;
	protected Rectangle bounds;

	protected void initialise(DetectedFace face) {
		DoGSIFTEngine engine = new DoGSIFTEngine();
		keys = engine.findFeatures(face.getFacePatch());
		bounds = face.getFacePatch().getBounds();
	}
	
	@Override
	public void readBinary(DataInput in) throws IOException {
		keys = MemoryLocalFeatureList.readNoHeader(in, Keypoint.class);
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		keys.writeBinary(out);
	}

	/**
	 * @return the keys
	 */
	public LocalFeatureList<Keypoint> getKeys() {
		return keys;
	}

	/**
	 * @return the bounds
	 */
	public Rectangle getBounds() {
		return bounds;
	}
}
