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

import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

import Jama.Matrix;

/**
 * A {@link FacialKeypoint} represents a keypoint on a face. Keypoints
 * are representative points found on all faces.
 * <p>
 * Keypoint types are based on Mark Everingham's 
 * <a href="http://www.robots.ox.ac.uk/~vgg/research/nface/">Oxford VGG 
 * Baseline Face Processing Code</a>
 *  
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FacialKeypoint implements ReadWriteableBinary {
	/**
	 * Types/locations of {@link FacialKeypoint}.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static enum FacialKeypointType {
		/**
		 * Left of the left eye
		 */
		EYE_LEFT_LEFT,
		/**
		 * Right of the left eye 
		 */
		EYE_LEFT_RIGHT,
		/**
		 * Left of the right eye
		 */
		EYE_RIGHT_LEFT,
		/**
		 * Right of the left eye
		 */
		EYE_RIGHT_RIGHT,
		/**
		 * Left-bottom of the nose
		 */
		NOSE_LEFT,
		/**
		 * Bottom-middle of the nose
		 */
		NOSE_MIDDLE,
		/**
		 * Bottom-right of the nose
		 */
		NOSE_RIGHT,
		/**
		 * Left corner of the mouth
		 */
		MOUTH_LEFT,
		/**
		 * Right corner of the mouth
		 */
		MOUTH_RIGHT,
		/**
		 * Centre of the left eye
		 */
		EYE_LEFT_CENTER,
		/**
		 * Centre of the right eye
		 */
		EYE_RIGHT_CENTER,
		/**
		 * Bridge of the nose
		 */
		NOSE_BRIDGE,
		/**
		 * Centre of the mouth
		 */
		MOUTH_CENTER
		;
		
		/**
		 * Get the {@link FacialKeypointType} at the specified ordinal
		 * @param ordinal the ordinal
		 * @return the corresponding {@link FacialKeypointType}
		 */
		public static FacialKeypointType valueOf(int ordinal) {
			return FacialKeypointType.values()[ordinal]; 
		}
	}

	/**
	 * The type of facial keypoint
	 */
	public FacialKeypointType type;
	
	/**
	 * The position of the keypoint in the image
	 */
	public Point2dImpl position;
	
	/**
	 * Default constructor. Sets type to {@link FacialKeypointType#EYE_LEFT_CENTER}
	 * and position to the origin.
	 */
	public FacialKeypoint() {
		this.type = FacialKeypointType.EYE_LEFT_CENTER;
		position = new Point2dImpl(0, 0);
	}
	
	/**
	 * Construct a FacialKeypoint at the origin with the specified type.
	 * @param type the type of facial keypoint
	 */
	public FacialKeypoint(FacialKeypointType type) {
		this.type = type;
		position = new Point2dImpl(0, 0);
	}
	
	/**
	 * Construct a FacialKeypoint with the specified type and position.
	 * @param type the type of facial keypoint
	 * @param pt the position in the image of the facial keypoint
	 */
	public FacialKeypoint(FacialKeypointType type, Point2d pt) {
		this.type = type;
		position = new Point2dImpl(pt);
	}
	
	protected void updatePosition(Matrix transform) {
		position = position.transform(transform);
	}
	
	protected static void updateImagePosition(FacialKeypoint[] kpts, Matrix transform) {
		for (FacialKeypoint k : kpts) k.updatePosition(transform);
	}

	/**
	 * Search the given points for the a keypoint with the 
	 * specified type and return it.
	 * 
	 * @param pts the points to search
	 * @param type the type of facial keypoint 
	 * @return the selected keypoint; or null if not found
	 */
	public static FacialKeypoint getKeypoint(FacialKeypoint[] pts, FacialKeypointType type) {
		for (FacialKeypoint fk : pts) {
			if (fk.type == type)
				return fk;
		}
		return null;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		type = FacialKeypointType.valueOf(in.readUTF());
		position.readBinary(in);
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeUTF(type.name());
		position.writeBinary(out);
	}
}
