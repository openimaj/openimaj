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
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.PixelSet;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;

/**
 * A {@link DetectedFace} that is represented/detected by a
 * {@link ConnectedComponent}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class CCDetectedFace extends DetectedFace {
	ConnectedComponent connectedComponent;

	/**
	 * Default constructor.
	 */
	public CCDetectedFace() {
		super();
	}

	/**
	 * Construct with a bounds rectangle (the bounding box of the face in the
	 * detection image),an image patch that describes the contents of the bounds
	 * rectangle from the original image, and a {@link ConnectedComponent}
	 * describing the shape of the detected face.
	 * 
	 * @param bounds
	 *            The bounding box of the face in the detection image
	 * @param patch
	 *            The subimage describing the contents of the bounding box.
	 * @param cc
	 *            The connected component representing the face.
	 * @param confidence
	 *            The confidence of the detection
	 */
	public CCDetectedFace(Rectangle bounds, FImage patch, ConnectedComponent cc, float confidence) {
		super(bounds, patch, confidence);
		this.connectedComponent = cc;
	}

	/**
	 * @return Get the connected component representing the face.
	 */
	public PixelSet getConnectedComponent() {
		return connectedComponent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.image.processing.face.detection.DetectedFace#writeBinary
	 * (java.io.DataOutput)
	 */
	@Override
	public void writeBinary(DataOutput out) throws IOException {
		super.writeBinary(out);
		connectedComponent.writeBinary(out);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.image.processing.face.detection.DetectedFace#binaryHeader()
	 */
	@Override
	public byte[] binaryHeader() {
		return "CCDF".getBytes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.image.processing.face.detection.DetectedFace#readBinary(
	 * java.io.DataInput)
	 */
	@Override
	public void readBinary(DataInput in) throws IOException {
		super.readBinary(in);
		connectedComponent.readBinary(in);
	}

	@Override
	public Shape getShape() {
		return connectedComponent.toPolygon();
	}
}
