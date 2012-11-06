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
package org.openimaj.feature.local.quantised;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.openimaj.feature.IntFV;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.Location;

/**
 * A QuantisedLocalFeature is a local feature with a single integer
 * feature-vector.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <L>
 *            the type of Location
 */
public class QuantisedLocalFeature<L extends Location> implements LocalFeature<L, IntFV> {
	/**
	 * The location of the local feature
	 */
	public L location;

	/**
	 * The identifier.
	 */
	public int id;

	/**
	 * Default constructor.
	 */
	public QuantisedLocalFeature() {
	}

	/**
	 * Construct the QuantisedLocalFeature with the given location and
	 * identifier.
	 * 
	 * @param location
	 *            the location.
	 * @param id
	 *            the identifier.
	 */
	public QuantisedLocalFeature(L location, int id) {
		this.location = location;
		this.id = id;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		location.writeBinary(out);
		out.writeInt(id);
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		location.writeASCII(out);
		out.println(id);
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		location.readBinary(in);
		id = in.readInt();
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		location.readASCII(in);
		id = Integer.parseInt(in.next());
	}

	@Override
	public byte[] binaryHeader() {
		return ("QLFI" + "." + new String(location.binaryHeader())).getBytes();
	}

	@Override
	public String asciiHeader() {
		return this.getClass().getName() + "." + location.asciiHeader();
	}

	@Override
	public IntFV getFeatureVector() {
		return new IntFV(new int[] { id });
	}

	@Override
	public L getLocation() {
		return location;
	}
}
