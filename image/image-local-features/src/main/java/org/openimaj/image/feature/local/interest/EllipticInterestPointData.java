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

import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;

import Jama.Matrix;

public class EllipticInterestPointData extends InterestPointData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3442580574124477236L;
	
	public Matrix transform;
	
	
	public void setTransform(Matrix transform) {
		this.transform = transform;
	}
	
	@Override
	public Matrix getTransform(){
		Matrix m = new Matrix(3,3);
		m.setMatrix(0, 1, 0,1,this.transform);
		m.set(0, 2, 0);
		m.set(1, 2, 0);
		m.set(2, 2, 1);
		return m;
	}
	
	@Override
	public Ellipse getEllipse() {
		return EllipseUtilities.fromTransformMatrix2x2(transform, x, y, scale);
	}
	
	@Override
	public void writeBinary(DataOutput out) throws IOException {
		super.writeBinary(out);
		out.writeFloat((float) transform.get(0,0));
		out.writeFloat((float) transform.get(0,1));
		out.writeFloat((float) transform.get(1,0));
		out.writeFloat((float) transform.get(1,1));
	}
	
	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		super.writeASCII(out);
		out.format(" %4.2f %4.2f %4.2f %4.2f", (float) transform.get(0,0),(float) transform.get(0,1),(float) transform.get(1,0),(float) transform.get(1,1));
	}
	
	@Override
	public void readBinary(DataInput in) throws IOException {
		super.readBinary(in);
		this.transform = new Matrix(2,2);
		this.transform.set(0, 0, in.readFloat());
		this.transform.set(0, 1, in.readFloat());
		this.transform.set(1, 0, in.readFloat());
		this.transform.set(1, 1, in.readFloat());
		this.setTransform(this.transform);
	}
	
	@Override
	public void readASCII(Scanner in) throws IOException {
		super.readASCII(in);
		this.transform = new Matrix(2,2);
		this.transform.set(0, 0, in.nextFloat());
		this.transform.set(0, 1, in.nextFloat());
		this.transform.set(1, 0, in.nextFloat());
		this.transform.set(1, 1, in.nextFloat());
		this.setTransform(this.transform);
	}
	
	@Override
	public EllipticInterestPointData clone() {
		EllipticInterestPointData d = (EllipticInterestPointData) super.clone();
		d.transform = this.transform.copy();
		return d;
	}
}
