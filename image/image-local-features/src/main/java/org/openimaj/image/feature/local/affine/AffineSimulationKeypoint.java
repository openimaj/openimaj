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
package org.openimaj.image.feature.local.affine;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.KeypointLocation;


public class AffineSimulationKeypoint extends Keypoint {
	private static final long serialVersionUID = 1L;
	
	public AffineParams affineParams;
	private int index;
	
	public AffineSimulationKeypoint(int length){
		super(length);
		this.affineParams = new AffineParams();
	}
	
	public AffineSimulationKeypoint(Keypoint k, AffineParams afParams, int index){
		super(k);
		this.affineParams = new AffineParams();
		this.affineParams.theta = afParams.theta;
		this.affineParams.tilt = afParams.tilt;
		this.index = index;
	}
	
	public static class AffineSimulationKeypointLocation extends KeypointLocation {
		private static final long serialVersionUID = 1L;
		
		public float theta;
		public float tilt;
		public int index;
		
		public AffineSimulationKeypointLocation() {
			super();
		}
		
		public AffineSimulationKeypointLocation(float x, float y, float scale, float ori, float theta, float tilt, int index) {
			super(x, y, ori, scale);
			this.theta = theta;
			this.tilt = tilt;
			this.index = index;
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			out.writeFloat(this.x);
			out.writeFloat(this.y);
			out.writeFloat(this.scale);
			out.writeFloat(this.orientation);
			out.writeFloat(theta);
			out.writeFloat(tilt);
			out.writeInt(index);
		}
		
		@Override
		public void writeASCII(PrintWriter out) throws IOException {
			/* Output data for the keypoint. */
			out.format("%4.2f %4.2f %4.2f %4.3f %4.3f %4.3f %d", y, x, scale, orientation, theta,tilt,index);
			out.println();
		}
		
		@Override
		public void readBinary(DataInput in) throws IOException {
			super.readBinary(in);
			theta = in.readFloat();
			tilt = in.readFloat();
			index = in.readInt();
		}
		
		@Override
		public void readASCII(Scanner in) throws IOException {
			super.readASCII(in);
			theta = in.nextFloat();
			tilt = in.nextFloat();
			String indexString = in.next();
			int dotIndex = indexString.indexOf(".");
			if(dotIndex!= -1) indexString = indexString.substring(0,dotIndex);
			index = Integer.parseInt(indexString);
		}
		
		@Override
		public Float getOrdinate(int dimension) {
			float [] pos = {x, y, scale, orientation, theta, tilt, index};
			return pos[dimension];
		}
	}

	
	@Override
	public AffineSimulationKeypointLocation getLocation() {
		return new AffineSimulationKeypointLocation(x, y, scale, ori, this.affineParams.theta, this.affineParams.tilt,this.index);
	}
	
	@Override
	public void setLocation(KeypointLocation location){
		super.setLocation(location);
		this.affineParams = new AffineParams();
		this.affineParams.theta = ((AffineSimulationKeypointLocation)location).theta;
		this.affineParams.tilt = ((AffineSimulationKeypointLocation)location).tilt;
		this.index = ((AffineSimulationKeypointLocation)location).index;
	}
	
	@Override
	public Float getOrdinate(int dimension) {
		float [] pos = {x, y, scale, ori, this.affineParams.theta, this.affineParams.tilt, index};
		return pos[dimension];
	}
	
	@Override
	public String toString() {
		return "AffineKeypoint(" + this.affineParams.theta + "," + this.affineParams.tilt + ","+ super.toString() + ")";
	}
}
