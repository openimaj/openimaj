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
package org.openimaj.image.feature.local.keypoints;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;


public class MinMaxKeypoint extends Keypoint {
	private static final long serialVersionUID = 1L;

	public boolean isMaxima;
	
	public MinMaxKeypoint() {
		super();
	}

	public MinMaxKeypoint(float x, float y, float ori, float scale, byte[] ivec, boolean isMaxima) {
		super(x, y, ori, scale, ivec);
		this.isMaxima = isMaxima;
	}

	public MinMaxKeypoint(int len) {
		super(len);
	}

	public static class MinMaxKeypointLocation extends KeypointLocation {
		private static final long serialVersionUID = 1L;
		
		public boolean isMaxima;

		public MinMaxKeypointLocation(float x, float y, float ori, float scale, boolean isMaxima) {
			super(x, y, ori, scale);
			this.isMaxima = isMaxima;
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			out.writeFloat(this.x);
			out.writeFloat(this.y);
			out.writeFloat(this.scale);
			out.writeFloat(this.orientation);
			out.writeBoolean(isMaxima);
		}
		
		@Override
		public void writeASCII(PrintWriter out) throws IOException {
			/* Output data for the keypoint. */
			out.format("%4.2f %4.2f %4.2f %4.3f %d", y, x, scale, orientation, isMaxima?1:0);
			out.println();
		}
		
		@Override
		public void readBinary(DataInput in) throws IOException {
			super.readBinary(in);
			isMaxima = in.readBoolean();
		}
		
		@Override
		public void readASCII(Scanner in) throws IOException {
			super.readASCII(in);
			isMaxima = in.nextInt() == 1;
		}
	}
	
	@Override
	public MinMaxKeypointLocation getLocation() {
		return new MinMaxKeypointLocation(x, y, ori, scale, isMaxima);
	}
	
	@Override
	public void setLocation(KeypointLocation location) {
		super.setLocation(location);
		if (location instanceof MinMaxKeypointLocation) this.isMaxima = ((MinMaxKeypointLocation)location).isMaxima;
	}
}
