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
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;

/**
 * A face detector that does nothing other than wrap the input image in a single
 * {@link DetectedFace} object.
 * <p>
 * This class is only likely to be useful for performing evaluations of
 * techniques that use datasets where a face has already been extracted/cropped
 * into an image.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE>
 *            The type of image
 */
public class IdentityFaceDetector<IMAGE extends Image<?, IMAGE>> implements FaceDetector<DetectedFace, IMAGE> {

	@Override
	public void readBinary(DataInput in) throws IOException {
		// Do nothing
	}

	@Override
	public byte[] binaryHeader() {
		return IdentityFaceDetector.class.getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		// Do nothing
	}

	@Override
	public List<DetectedFace> detectFaces(IMAGE image) {
		DetectedFace face = null;
		final Object oimage = image;

		if (oimage instanceof FImage)
			face = new DetectedFace(image.getBounds(), ((FImage) (oimage)), 1);
		else if (oimage instanceof MBFImage)
			face = new DetectedFace(image.getBounds(), ((MBFImage) (oimage)).flatten(), 1);
		else
			throw new RuntimeException("unsupported image type");

		final List<DetectedFace> faces = new ArrayList<DetectedFace>(1);
		faces.add(face);

		return faces;
	}

	@Override
	public String toString() {
		return "Identity Face Detector";
	}
}
