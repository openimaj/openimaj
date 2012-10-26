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
import org.openimaj.math.geometry.shape.Shape;

/**
 * A DetectedFace models a face detected by a face detector, together with the
 * locations of certain facial features localised on the face.
 * 
 * @author Jonathon Hare
 * 
 */
public class DetectedFace implements ReadWriteableBinary {
	/**
	 * The upright bounds of the face in the image in which it was detected
	 */
	protected Rectangle bounds;

	/**
	 * The extracted sub-image representing the face. This is normally extracted
	 * directly from the bounds rectangle in the original image, but in some
	 * cases might be extracted from a sub-region of the bounds rectangle.
	 */
	protected FImage facePatch;

	/**
	 * The confidence of the detection; higher numbers mean higher confidence.
	 */
	protected float confidence = 1;

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
	 * @param bounds
	 *            The bounding box of the face in the detection image.
	 * @param patch
	 *            The subimage describing the contents of the bounding box.
	 * @param confidence
	 *            The confidence of the detection.
	 */
	public DetectedFace(Rectangle bounds, FImage patch, float confidence) {
		this.bounds = bounds;
		this.facePatch = patch;
		this.confidence = confidence;
	}

	/**
	 * @return Get the sub-image representing the detected face
	 */
	public FImage getFacePatch() {
		return facePatch;
	}

	/**
	 * Get the bounding box of the face in the detection image. This might be
	 * the same as the shape returned by {@link #getShape()}, or it might
	 * encompass that shape.
	 * 
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

	/**
	 * Get the confidence of the detection. Higher numbers mean higher
	 * confidence.
	 * 
	 * @return the confidence.
	 */
	public float getConfidence() {
		return confidence;
	}

	/**
	 * Get the shape of the detection. In most cases, this will just return the
	 * bounds rectangle, however, subclasses can override to return a better
	 * geometric description of the detection area.
	 * 
	 * @return the shape describing the detection in the original image.
	 */
	public Shape getShape() {
		return bounds;
	}

	/**
	 * Set the confidence of the detection. Higher numbers mean higher
	 * confidence.
	 * 
	 * @param confidence
	 *            the confidence.
	 */
	public void setConfidence(int confidence) {
		this.confidence = confidence;
	}
}
