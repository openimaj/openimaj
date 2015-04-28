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
package org.openimaj.math.geometry.point;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * A concrete implementation of a {@link Coordinate} that has an associated
 * payload.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T>
 *            Type of underlying coordinate
 * @param <O>
 *            Type of payload data
 */
public class PayloadCoordinate<T extends Coordinate, O> implements Coordinate {

	private T coord;
	private O payload;

	/**
	 * Construct with coordinate and payload.
	 * 
	 * @param coord
	 *            The coordinate.
	 * @param payload
	 *            The payload.
	 */
	public PayloadCoordinate(T coord, O payload) {
		this.coord = coord;
		this.setPayload(payload);
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		coord.readASCII(in);
	}

	@Override
	public String asciiHeader() {
		return coord.asciiHeader();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		coord.readBinary(in);
	}

	@Override
	public byte[] binaryHeader() {
		return coord.binaryHeader();
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		coord.writeASCII(out);
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		coord.writeBinary(out);
	}

	@Override
	public Number getOrdinate(int dimension) {
		return coord.getOrdinate(dimension);
	}

	@Override
	public int getDimensions() {
		return coord.getDimensions();
	}

	/**
	 * Set the payload.
	 * 
	 * @param payload
	 *            The payload
	 */
	public void setPayload(O payload) {
		this.payload = payload;
	}

	/**
	 * @return The payload.
	 */
	public O getPayload() {
		return payload;
	}

	/**
	 * Create a {@link PayloadCoordinate}.
	 * 
	 * @param <T>
	 *            The coordinate type.
	 * @param <O>
	 *            The payload type.
	 * @param coord
	 *            The coordinate.
	 * @param payload
	 *            The payload.
	 * @return the newly created {@link PayloadCoordinate}.
	 */
	public static <T extends Coordinate, O> PayloadCoordinate<T, O> payload(T coord, O payload) {
		return new PayloadCoordinate<T, O>(coord, payload);
	}

	@Override
	public void setOrdinate(int dimension, Number value) {
		coord.setOrdinate(dimension, value);
	}
}
