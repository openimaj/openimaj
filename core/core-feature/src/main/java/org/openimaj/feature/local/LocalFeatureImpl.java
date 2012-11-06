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

import org.openimaj.feature.FeatureVector;

/**
 * A basic implementation of a {@link LocalFeature} that internally holds
 * references to a {@link FeatureVector} and {@link Location}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <L>
 *            The type of {@link Location}
 * @param <F>
 *            The type of {@link FeatureVector}
 */
public class LocalFeatureImpl<L extends Location, F extends FeatureVector> implements LocalFeature<L, F> {
	/** The {@link Location} of the local feature */
	public L location;

	/** The {@link FeatureVector} of the local feature */
	public F feature;

	/**
	 * Construct the LocalFeatureImpl with the given {@link Location} and
	 * {@link FeatureVector}.
	 * 
	 * @param location
	 *            the location
	 * @param feature
	 *            the feature vector
	 */
	public LocalFeatureImpl(L location, F feature) {
		this.location = location;
		this.feature = feature;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		location.writeBinary(out);
		feature.writeBinary(out);
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		location.writeASCII(out);
		feature.writeASCII(out);
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		location.readBinary(in);
		feature.readBinary(in);
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		location.readASCII(in);
		feature.readASCII(in);
	}

	@Override
	public byte[] binaryHeader() {
		return ("LFVI" + "." + new String(location.binaryHeader()) + "." + new String(feature.asciiHeader())).getBytes();
	}

	@Override
	public String asciiHeader() {
		return this.getClass().getName() + "." + location.asciiHeader() + "." + feature.asciiHeader();
	}

	@Override
	public F getFeatureVector() {
		return feature;
	}

	@Override
	public L getLocation() {
		return location;
	}
}
