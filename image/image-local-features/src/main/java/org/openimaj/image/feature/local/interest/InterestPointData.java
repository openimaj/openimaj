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
package org.openimaj.image.feature.local.interest;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.openimaj.feature.local.ScaleSpaceLocation;
import org.openimaj.math.geometry.shape.Ellipse;

import Jama.Matrix;

public class InterestPointData extends ScaleSpaceLocation{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6710204268210799061L;
	public float score;
	
	
	public InterestPointData(){
		
	}
	
	@Override
	public InterestPointData clone() {
		InterestPointData d = (InterestPointData) super.clone();
		d.score = this.score;
		return d;
	}
	
	boolean equalPos(InterestPointData otherPos){
		return otherPos.x == this.x && otherPos.y == this.y && otherPos.scale == this.scale;
	}
	
	@Override
	public boolean equals(Object other) {
		return super.equals(other) && this.score == ((InterestPointData)other).score;
	}

	public Ellipse getEllipse() {
		return new Ellipse(x, y, scale*scale,scale*scale,0);
	}
	
	public Matrix getTransform() {
		return Matrix.identity(3, 3);
	}
	
	@Override
	public void writeBinary(DataOutput out) throws IOException {
		super.writeBinary(out);
		out.writeFloat(score);
	}
	
	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		super.writeASCII(out);
		out.format(" %4.2f", this.score);
	}
	
	@Override
	public void readBinary(DataInput in) throws IOException {
		super.readBinary(in);
		this.score = in.readFloat();
	}
	
	@Override
	public void readASCII(Scanner in) throws IOException {
		super.readASCII(in);
		this.score = in.nextFloat();
	}

	
}