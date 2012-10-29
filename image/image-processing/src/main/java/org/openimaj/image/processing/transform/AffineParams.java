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
package org.openimaj.image.processing.transform;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.io.ReadWriteable;

/**
 * Parameters defining an affine simulation, in terms of a tilt and rotation.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Morel, Jean-Michel", "Yu, Guoshen" },
		title = "{ASIFT: A New Framework for Fully Affine Invariant Image Comparison}",
		year = "2009",
		journal = "SIAM J. Img. Sci.",
		publisher = "Society for Industrial and Applied Mathematics")
public class AffineParams implements ReadWriteable {
	/**
	 * The angle of rotation
	 */
	public float theta;

	/**
	 * The amount of tilt
	 */
	public float tilt;

	/**
	 * Construct with the given rotation and tilt.
	 * 
	 * @param theta
	 *            the angle of rotation
	 * @param tilt
	 *            the amount of tilt
	 */
	public AffineParams(float theta, float tilt) {
		this.theta = theta;
		this.tilt = tilt;
	}

	/**
	 * Construct with zero tilt and rotation
	 */
	public AffineParams() {
	}

	@Override
	public boolean equals(Object po) {
		if (po instanceof AffineParams) {
			final AffineParams p = (AffineParams) po;
			return (Math.abs(theta - p.theta) < 0.00001 && Math.abs(tilt - p.tilt) < 0.00001);
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int hash = new Float(theta).hashCode() ^ new Float(tilt).hashCode();
		return hash;
	}

	@Override
	public String toString() {
		return String.format("theta:%f tilt:%f", theta, tilt);
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		this.theta = in.readFloat();
		this.tilt = in.readFloat();
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		this.theta = in.nextFloat();
		this.tilt = in.nextFloat();
	}

	@Override
	public byte[] binaryHeader() {
		return "".getBytes();
	}

	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeFloat(this.theta);
		out.writeFloat(this.tilt);
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.println(this.theta);
		out.println(this.tilt);
	}
}
