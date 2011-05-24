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
package org.openimaj.feature.local.keypoints.face;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.openimaj.feature.local.Location;
import org.openimaj.image.pixel.Pixel;

import Jama.Matrix;

public class FacialLocation implements Location {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	List<FacialFeatureLocation> featureLocations;
	Matrix transform;
	
	public FacialLocation() {
		this.featureLocations = new ArrayList<FacialFeatureLocation>();
	}
	public FacialLocation(FacialDescriptor facialKeypoint) {
		this.transform = facialKeypoint.transform;
		this.featureLocations = new ArrayList<FacialFeatureLocation>();
		if(facialKeypoint.featurePoints!=null)
			for(Pixel featureKeypoint : facialKeypoint.featurePoints){
				featureLocations.add(new FacialFeatureLocation(featureKeypoint));
			}
	}

	@Override
	public byte[] binaryHeader() {
		return "".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeDouble(transform.get(0, 0));
		out.writeDouble(transform.get(0, 1));
		out.writeDouble(transform.get(1, 0));
		out.writeDouble(transform.get(1, 1));
		out.writeInt(this.featureLocations.size());
		for(FacialFeatureLocation fl : this.featureLocations){
			fl.writeBinary(out);
		}
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.print(transform.get(0, 0) + " ");
		out.print(transform.get(0, 1) + " ");
		out.print(transform.get(1, 0) + " ");
		out.println(transform.get(1, 1));
		out.println(this.featureLocations.size());
		for(FacialFeatureLocation fl : this.featureLocations){
			fl.writeASCII(out);
			out.print(" ");
		}
	}
	
	@Override
	public Location readBinary(DataInput in) throws IOException {
		transform = new Matrix(2,2);
		transform.set(0, 0, in.readDouble());
		transform.set(0, 1, in.readDouble());
		transform.set(1, 0, in.readDouble());
		transform.set(1, 1, in.readDouble());
		int nFeatures = in.readInt();
		for(int i = 0; i < nFeatures; i++) {
			FacialFeatureLocation f = new FacialFeatureLocation();
			this.featureLocations.add((FacialFeatureLocation) f.readBinary(in));
		}
		return this;
	}

	@Override
	public Location readASCII(Scanner in) throws IOException {
		transform = new Matrix(2,2);
		transform.set(0, 0, in.nextDouble());
		transform.set(0, 1, in.nextDouble());
		transform.set(1, 0, in.nextDouble());
		transform.set(1, 1, in.nextDouble());
		int nFeatures = in.nextInt();
		for(int i = 0; i < nFeatures; i++) {
			FacialFeatureLocation f = new FacialFeatureLocation();
			this.featureLocations.add((FacialFeatureLocation) f.readASCII(in));
		}
		return this;
	}
	
	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public Number getOrdinate(int dimension) {
		int d = dimension / 2;
		int i = dimension - (d * 2);
		return this.featureLocations.get(d).getOrdinate(i);
	}

	@Override
	public int getDimensions() {
		return 2 * this.featureLocations.size();
	}
}
