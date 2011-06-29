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
package org.openimaj.image.processing.face.keypoints;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

import Jama.Matrix;

public class FacialKeypoint implements ReadWriteableBinary {
	public static enum FacialKeypointType {
		EYE_LEFT_LEFT,
		EYE_LEFT_RIGHT,
		EYE_RIGHT_LEFT,
		EYE_RIGHT_RIGHT,
		NOSE_LEFT,
		NOSE_MIDDLE,
		NOSE_RIGHT,
		MOUTH_LEFT,
		MOUTH_RIGHT,
		EYE_LEFT_CENTER,
		EYE_RIGHT_CENTER,
		NOSE_BRIDGE,
		MOUTH_CENTER
		;
		
		public static FacialKeypointType valueOf(int ordinal) {
			return FacialKeypointType.values()[ordinal]; 
		}
	}

	public FacialKeypointType type;
	public Point2dImpl position;
	
	public FacialKeypoint() {
		this.type = FacialKeypointType.EYE_LEFT_CENTER;
		position = new Point2dImpl(0, 0);
	}
	
	public FacialKeypoint(FacialKeypointType type) {
		this.type = type;
		position = new Point2dImpl(0, 0);
	}
	
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
