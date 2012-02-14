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
package org.openimaj.image.processing.face.detection;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * A DetectedFace models a face detected by a face detector,
 * together with the locations of certain facial features
 * localised on the face.
 * 
 * @author Jonathon Hare
 *
 */
public class DetectedFace implements ReadWriteableBinary {
	/**
	 * The bounds of the face in the image in which it was detected
	 */
	protected Rectangle bounds;

	/**
	 * The extracted sub-image representing the face. This is extracted
	 * directly from the bounds rectangle in the original image. 
	 */
	protected FImage facePatch;
	
	/**
	 * Default constructor with an empty rectangle as bounds
	 */
	public DetectedFace() {
		bounds = new Rectangle();
	}
	
	/**
	 * Construct with a bounds rectangle (the bounding box of the face in the 
	 * detection image) and an image patch that describes the contents of the
	 * bounds rectangle from the original image.
	 * 
	 * @param bounds The bounding box of the face in the detection image
	 * @param patch The subimage describing the contents of the bounding box.
	 */
	public DetectedFace(Rectangle bounds, FImage patch) {
		this.bounds = bounds;
		this.facePatch = patch;
	}
	
	/**
	 * @return Get the sub-image representing the detected face
	 */
	public FImage getFacePatch() {
		return facePatch;
	}

	/**
	 * @return The bounding box of the face in the detection image
	 */
	public Rectangle getBounds() {
		return bounds;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		bounds.writeBinary(out);
		ImageUtilities.write(facePatch, "png", out);
	}

	@Override
	public byte[] binaryHeader() {
		return "DF".getBytes();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		bounds.readBinary(in);
		facePatch = ImageUtilities.readF(in);
	}
}
