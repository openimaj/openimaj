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

import org.openimaj.feature.OrientedFeatureVector;
import org.openimaj.image.feature.local.interest.InterestPointData;

/**
 * An oriented feature with at a location defined by an
 * {@link InterestPointData}.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            The type of {@link InterestPointData}
 */
public abstract class InterestPointKeypoint<T extends InterestPointData> extends Keypoint {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The feature location
	 */
	public T location;

	/**
	 * Construct with a <code>null</code> location and default length feature
	 */
	public InterestPointKeypoint() {
	}

	/**
	 * Construct with a <code>null</code> location and feature of the given
	 * length
	 * 
	 * @param length
	 *            the feature length
	 */
	public InterestPointKeypoint(int length) {
		super(length);
	}

	/**
	 * @param featureVector
	 *            the feature vector containing orientation and the byte[]
	 * @param point
	 *            the location and shape of the interest point
	 */
	public InterestPointKeypoint(OrientedFeatureVector featureVector, T point) {
		this.ivec = featureVector.values.clone();
		this.location = point;
		this.x = this.location.x;
		this.y = this.location.y;
		this.scale = this.location.scale;
		this.ori = featureVector.orientation;
	}

	/**
	 * @return create a new {@link InterestPointData} compatible with this
	 *         feature
	 */
	public abstract T createEmptyLocation();

	@Override
	public void readBinary(DataInput in) throws IOException {
		super.readBinary(in);
		location = createEmptyLocation();
		location.readBinary(in);
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		super.readASCII(in);
		location = createEmptyLocation();
		location.readASCII(in);
	}

	@Override
	public byte[] binaryHeader() {
		return super.binaryHeader();
	}

	@Override
	public String asciiHeader() {
		return super.asciiHeader();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		super.writeBinary(out);
		this.location.writeBinary(out);

	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		super.writeASCII(out);
		this.location.writeASCII(out);
	}
}
