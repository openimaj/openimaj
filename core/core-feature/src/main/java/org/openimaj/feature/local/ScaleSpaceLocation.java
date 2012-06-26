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
package org.openimaj.feature.local;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.openimaj.math.geometry.point.ScaleSpacePoint;

/**
 * ScaleSpaceLocation represents a {@link Location} in scale-space.
 * ScaleSpaceLocations contain x, y and scale ordinates.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ScaleSpaceLocation extends SpatialLocation implements ScaleSpacePoint, Cloneable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * the scale
	 */
	public float scale;
	
	/**
	 * Construct the ScaleSpaceLocation at 0, 0, 0.
	 */
	public ScaleSpaceLocation() {
		super(0, 0);
	}
	
	/**
	 * Construct the ScaleSpaceLocation with the given x, y and 
	 * scale coordinates.
	 * @param x the x-coordinate
	 * @param y the y-coordinate
	 * @param scale the scale coordinate
	 */
	public ScaleSpaceLocation(float x, float y, float scale) {
		super(x, y);
		this.scale = scale;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeFloat(this.x);
		out.writeFloat(this.y);
		out.writeFloat(this.scale);
	}
	
	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		//for legacy reasons ascii format writes y, x, scale
		out.format("%4.2f %4.2f %4.2f", y, x, scale);
		out.println();
	}
	
	@Override
	public void readBinary(DataInput in) throws IOException {
		x = in.readFloat();
		y = in.readFloat();
		scale = in.readFloat();
	}
	
	@Override
	public void readASCII(Scanner in) throws IOException {
		y = Float.parseFloat(in.next());
		x = Float.parseFloat(in.next());
		scale = Float.parseFloat(in.next());
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
		float [] pos = {x, y, scale};
		return pos[dimension];
	}
	
	@Override
	public int getDimensions() {
		return 3;
	}

	@Override
	public float getScale() {
		return scale;
	}

	@Override
	public void setScale(float scale) {
		this.scale = scale;
	}
	
	@Override
	public ScaleSpaceLocation clone(){
		ScaleSpaceLocation l = (ScaleSpaceLocation) super.clone();
		l.scale = this.scale;
		return l;
	}
}
