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
package org.openimaj.image.processing.face.alignment;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.FacialKeypoint;
import org.openimaj.image.processing.face.detection.keypoints.FacialKeypoint.FacialKeypointType;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

/**
 * Attempt to align a face by rotating and scaling it. Facial Keypoints are used
 * to judge the alignment. Specifically, the distance between the eyes is
 * normalised by scaling, and the eyes are rotated to be level. The face is then
 * translated to a known position (again, based on the eyes).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class RotateScaleAligner implements FaceAligner<KEDetectedFace> {
	private static final FImage DEFAULT_MASK = loadDefaultMask();

	// Define the geometry
	private int eyeDist = 68;
	private int eyePaddingLeftRight = 6;
	private int eyePaddingTop = 20;

	private FImage mask = DEFAULT_MASK;

	/**
	 * Default constructor with no mask.
	 */
	public RotateScaleAligner() {
	}

	/**
	 * Default constructor with no mask.
	 * 
	 * @param targetSize
	 *            target aligned image size
	 */
	public RotateScaleAligner(int targetSize) {
		final int canonicalSize = 2 * eyePaddingLeftRight + eyeDist;

		final double sf = targetSize / canonicalSize;

		eyeDist = (int) (eyeDist * sf);
		eyePaddingLeftRight = (targetSize - eyeDist) / 2;
		eyePaddingTop = (int) (eyePaddingTop * sf);
	}

	/**
	 * Construct with a mask (in the canonical frame) to apply after alignment.
	 * 
	 * @param mask
	 *            The mask.
	 */
	public RotateScaleAligner(FImage mask) {
		this.mask = mask;
	}

	@Override
	public FImage align(KEDetectedFace descriptor) {
		final FacialKeypoint lefteye = descriptor.getKeypoint(FacialKeypointType.EYE_LEFT_LEFT);
		final FacialKeypoint righteye = descriptor.getKeypoint(FacialKeypointType.EYE_RIGHT_RIGHT);

		final float dx = righteye.position.x - lefteye.position.x;
		final float dy = righteye.position.y - lefteye.position.y;

		final float rotation = (float) Math.atan2(dy, dx);
		final float scaling = (float) (eyeDist / Math.sqrt(dx * dx + dy * dy));

		final float tx = lefteye.position.x - eyePaddingLeftRight / scaling;
		final float ty = lefteye.position.y - eyePaddingTop / scaling;

		final Matrix tf0 = TransformUtilities.scaleMatrix(scaling, scaling)
				.times(TransformUtilities.translateMatrix(-tx, -ty))
				.times(TransformUtilities.rotationMatrixAboutPoint(-rotation, lefteye.position.x, lefteye.position.y));
		final Matrix tf = tf0.inverse();

		final FImage J = FKEFaceDetector.pyramidResize(descriptor.getFacePatch(), tf);
		return FKEFaceDetector.extractPatch(J, tf, 2 * eyePaddingLeftRight + eyeDist, 0);
	}

	private static FImage loadDefaultMask() {
		try {
			return ImageUtilities.readF(FaceAligner.class.getResourceAsStream("affineMask.png"));
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public FImage getMask() {
		return mask;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		eyeDist = in.readInt();
		eyePaddingLeftRight = in.readInt();
		eyePaddingTop = in.readInt();

		mask = ImageUtilities.readF(in);
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(eyeDist);
		out.writeInt(eyePaddingLeftRight);
		out.writeInt(eyePaddingTop);

		ImageUtilities.write(mask, "png", out);
	}
}
