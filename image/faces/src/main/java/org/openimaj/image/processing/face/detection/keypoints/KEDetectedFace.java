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
package org.openimaj.image.processing.face.detection.keypoints;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.keypoints.FacialKeypoint.FacialKeypointType;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * A K(eypoint)E(nriched)DetectedFace models a face detected by a face detector,
 * together with the locations of certain facial features localised on the face.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class KEDetectedFace extends DetectedFace {
	/**
	 * A list of detected facial keypoints. The coordinates are given in terms
	 * of the facePatch image. To project them into the original image you need
	 * to translate by bounds.x and bounds.y.
	 */
	protected FacialKeypoint[] keypoints;

	/**
	 * Default constructor
	 */
	public KEDetectedFace() {
		super();
	}

	/**
	 * Construct with parameters.
	 * 
	 * @param bounds
	 *            the bounds rectangle in the detection image
	 * @param patch
	 *            the patch
	 * @param keypoints
	 *            the detected facial keypoints.
	 * @param confidence
	 *            the confidence of detection
	 */
	public KEDetectedFace(Rectangle bounds, FImage patch, FacialKeypoint[] keypoints, float confidence) {
		super(bounds, patch, confidence);

		this.keypoints = keypoints;
	}

	/**
	 * Get a keypoint of the specified type.
	 * 
	 * @param type
	 *            the type of keypoint
	 * @return the keypoint, or null if it wasn't found
	 */
	public FacialKeypoint getKeypoint(FacialKeypointType type) {
		return FacialKeypoint.getKeypoint(keypoints, type);
	}

	/**
	 * Get a keypoint of the specified type, interpolating the position if the
	 * internal model doesn't have a matching point. Currently only the
	 * {@link FacialKeypointType#MOUTH_CENTER},
	 * {@link FacialKeypointType#EYE_LEFT_CENTER} and
	 * {@link FacialKeypointType#EYE_RIGHT_CENTER} points are supported iff the
	 * corresponding left and right points are available.
	 * 
	 * @param type
	 *            the type of keypoint
	 * @return the keypoint, or null if it wasn't found
	 */
	public FacialKeypoint getKeypointInterpolated(FacialKeypointType type) {
		final FacialKeypoint kpt = getKeypoint(type);

		if (kpt == null) {
			switch (type) {
			case EYE_LEFT_CENTER:
				return createInterpolated(type, getKeypoint(FacialKeypointType.EYE_LEFT_LEFT),
						getKeypoint(FacialKeypointType.EYE_LEFT_RIGHT));
			case EYE_RIGHT_CENTER:
				return createInterpolated(type, getKeypoint(FacialKeypointType.EYE_RIGHT_LEFT),
						getKeypoint(FacialKeypointType.EYE_RIGHT_RIGHT));
			case MOUTH_CENTER:
				return createInterpolated(type, getKeypoint(FacialKeypointType.MOUTH_LEFT),
						getKeypoint(FacialKeypointType.MOUTH_RIGHT));
			default:
				break;
			}
		}
		return null;
	}

	private FacialKeypoint createInterpolated(FacialKeypointType type, FacialKeypoint left, FacialKeypoint right)
	{
		if (left == null || right == null)
			return null;

		final float x = right.position.x - left.position.x;
		final float y = right.position.y - left.position.y;

		return new FacialKeypoint(type, new Point2dImpl(x, y));
	}

	/**
	 * @return the {@link FacialKeypoint}s associated with this detection
	 */
	public FacialKeypoint[] getKeypoints() {
		return keypoints;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		super.writeBinary(out);

		out.writeInt(keypoints.length);
		for (final FacialKeypoint k : keypoints)
			k.writeBinary(out);
	}

	@Override
	public byte[] binaryHeader() {
		return "KEDF".getBytes();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		super.readBinary(in);

		final int sz = in.readInt();
		keypoints = new FacialKeypoint[sz];
		for (int i = 0; i < sz; i++) {
			keypoints[i] = new FacialKeypoint();
			keypoints[i].readBinary(in);
		}
	}
}
