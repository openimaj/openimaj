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
package org.openimaj.feature;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * A feature-vector representation of an enumeration
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T>
 *            type of underlying enumeration
 */
public class EnumFV<T extends Enum<T>> implements FeatureVector {
	private static final long serialVersionUID = 1L;
	private T enumValue;

	/**
	 * Construct the feature with the given value
	 *
	 * @param value
	 *            the value
	 */
	public EnumFV(T value) {
		this.enumValue = value;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		try {
			@SuppressWarnings("unchecked")
			final Class<T> clz = (Class<T>) Class.forName(in.readUTF());
			final String name = in.readUTF();
			this.enumValue = Enum.valueOf(clz, name);
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		try {
			@SuppressWarnings("unchecked")
			final Class<T> clz = (Class<T>) Class.forName(in.nextLine());
			final String name = in.nextLine();
			this.enumValue = Enum.valueOf(clz, name);
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] binaryHeader() {
		return "EnFV".getBytes();
	}

	@Override
	public String asciiHeader() {
		return getClass().getName() + " ";
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeUTF(enumValue.getDeclaringClass().getName());
		out.writeUTF(enumValue.toString());
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.println(enumValue.getDeclaringClass().getName());
		out.println(enumValue.toString());
	}

	@Override
	public Enum<T> getVector() {
		return enumValue;
	}

	@Override
	public int length() {
		return enumValue.getDeclaringClass().getEnumConstants().length;
	}

	@Override
	public DoubleFV normaliseFV(double[] min, double[] max) {
		return asDoubleFV().normaliseFV(min, max);
	}

	@Override
	public DoubleFV normaliseFV(double min, double max) {
		return asDoubleFV().normaliseFV(min, max);
	}

	@Override
	public DoubleFV normaliseFV() {
		return asDoubleFV().normaliseFV();
	}

	@Override
	public DoubleFV normaliseFV(double p) {
		return asDoubleFV().normaliseFV(p);
	}

	@Override
	public DoubleFV asDoubleFV() {
		return new DoubleFV(asDoubleVector());
	}

	@Override
	public double[] asDoubleVector() {
		final double[] vec = new double[length()];
		vec[enumValue.ordinal()] = 1;
		return vec;
	}

	@Override
	public double getAsDouble(int i) {
		if (enumValue.ordinal() == i)
			return 1;
		return 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setFromDouble(int i, double v) {
		if (v == 1) {
			try {
				enumValue = (T) this.getClass().getField("enumValue").getType().getEnumConstants()[i];
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public EnumFV<T> newInstance() {
		return new EnumFV<T>(enumValue);
	}
}
