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

import org.openimaj.feature.local.ScaleSpaceLocation;

/**
 * The location of a {@link Keypoint}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class KeypointLocation extends ScaleSpaceLocation {
	private static final long serialVersionUID = 1L;

	/**
	 * The dominant orientation of the {@link Keypoint}
	 */
	public float orientation;

	/**
	 * Construct with zero location, orientation and scale.
	 */
	public KeypointLocation() {
	}

	/**
	 * Construct with the given parameters
	 * 
	 * @param x
	 *            x-ordinate of feature
	 * @param y
	 *            y-ordinate of feature
	 * @param scale
	 *            scale of feature
	 * @param orientation
	 *            orientation of feature
	 */
	public KeypointLocation(float x, float y, float orientation, float scale) {
		super(x, y, scale);
		this.orientation = orientation;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeFloat(this.x);
		out.writeFloat(this.y);
		out.writeFloat(this.scale);
		out.writeFloat(this.orientation);
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		// for legacy reasons ascii format writes y, x, scale, ori
		out.format("%4.2f %4.2f %4.2f %4.3f", y, x, scale, orientation);
		out.println();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		super.readBinary(in);
		orientation = in.readFloat();
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		super.readASCII(in);
		orientation = Float.parseFloat(in.next());
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
	public Float getOrdinate(int dimension) {
		final float[] pos = { x, y, scale, orientation };
		return pos[dimension];
	}

	@Override
	public int getDimensions() {
		return 3;
	}
}
