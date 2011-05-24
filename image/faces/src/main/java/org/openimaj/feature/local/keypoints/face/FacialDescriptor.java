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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Scanner;

import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.FloatFV;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.Location;
import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.io.VariableLength;
import org.openimaj.math.geometry.point.Coordinate;

import Jama.Matrix;


/**
 * 
 * @author Jonathon Hare
 *
 */
public class FacialDescriptor implements Serializable, Coordinate, LocalFeature, VariableLength {
	static final long serialVersionUID = 1234554345;
	public float[] featureVector;
	/* Number of sub facial features encoded */
	public int nFeatures;
	/* Length of feature facial feature. featureVector.length = nFeatures * featureLength*/
	public int featureLength;
	
	/* Central location of each facial feature extracted */
	public ArrayList<Pixel> featurePoints;
	
	/* Affine projection from flat,vertically oriented face to located face space*/
	public Matrix transform;
	public int featureRadius;
	public FImage facePatch;
	
	
	public FacialDescriptor(int length){
		featureVector = new float[length];
	}

	public FacialDescriptor() {}

	@Override
	public byte[] binaryHeader() {
		return "".getBytes();
	}
	@Override
	public void writeBinary(DataOutput out) throws IOException {
		getLocation().writeBinary(out);
		out.writeInt(this.featureLength);
		out.writeInt(this.featureRadius);
		for(float f : featureVector){
			out.writeFloat(f);
		}
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		/* Output data for the keypoint. */
		getLocation().writeASCII(out);
		out.println(this.featureLength + " " + this.featureRadius);
		for (int i = 0; i < featureVector.length; i++) {
			if (i>0 && i % 20 == 0)
				out.println();
			out.print(" " + featureVector[i]);
		}
		out.println();
	}
	
	@Override
	public String asciiHeader() {
		return "";
	}
	
	@Override
	public LocalFeature readBinary(DataInput in) throws IOException {
		setLocation((FacialLocation) getLocation().readBinary(in));
		this.nFeatures = this.featurePoints.size();
		this.featureLength = in.readInt();
		this.featureRadius = in.readInt();
		this.featureVector = new float[nFeatures * featureLength];
		for(int i = 0; i < featureVector.length; i++) this.featureVector[i] = in.readFloat();
		return this;
	}

	@Override
	public LocalFeature readASCII(Scanner in) throws IOException {
		setLocation((FacialLocation) getLocation().readASCII(in));
		this.nFeatures = this.featurePoints.size();
		this.featureLength = in.nextInt();
		this.featureRadius = in.nextInt();
		this.featureVector = new float[nFeatures * featureLength];
		for(int i = 0; i < featureVector.length; i++) this.featureVector[i] = in.nextFloat();
		return this;
	}

	@Override
	public FeatureVector getFeatureVector() {
		return new FloatFV(this.featureVector);
	}

	@Override
	public Location getLocation() {
		return new FacialLocation(this);
	}
	
	private void setLocation(FacialLocation fl) {
		this.featurePoints = new ArrayList<Pixel>();
		this.nFeatures = fl.featureLocations.size();
		for(int i = 0; i < this.nFeatures ; i++){
			this.featurePoints.add(new Pixel(
				fl.featureLocations.get(i).getOrdinate(0),
				fl.featureLocations.get(i).getOrdinate(1)
			));
		}
		this.transform = fl.transform;
	}

	@Override
	public Number getOrdinate(int dimension) {
		return null;
	}

	@Override
	public int getDimensions() {
		return 0;
	}
}
