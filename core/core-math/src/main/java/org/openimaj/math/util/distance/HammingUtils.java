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
package org.openimaj.math.util.distance;

/**
 * Utilities for hamming distance calculations. All hamming distance
 * calculations between native types are decomposed into the summation over all
 * bytes in the native type. The hamming distance of byte types is computed
 * through a lookup table.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class HammingUtils {
	private static int[] BYTE_BIT_COUNTS;
	static {
		BYTE_BIT_COUNTS = new int[256];

		for (int i = 0; i < 256; i++) {
			BYTE_BIT_COUNTS[i] = (i & 1) + BYTE_BIT_COUNTS[i / 2];
		}
	}

	private final static int LONG_BYTES = Long.SIZE / Byte.SIZE;
	private final static int INT_BYTES = Integer.SIZE / Byte.SIZE;
	private final static int SHORT_BYTES = Short.SIZE / Byte.SIZE;
	private final static int CHAR_BYTES = Character.SIZE / Byte.SIZE;

	/**
	 * Bitwise (assuming packed bit strings) hamming distance
	 * 
	 * @param i1
	 *            first bit string
	 * @param i2
	 *            second bit string
	 * @return the hamming distance
	 */
	public static int packedHamming(double i1, double i2) {
		final long i1l = Double.doubleToRawLongBits(i1);
		final long i2l = Double.doubleToRawLongBits(i2);

		return packedHamming(i1l, i2l);
	}

	/**
	 * Bitwise (assuming packed bit strings) hamming distance
	 * 
	 * @param i1
	 *            first bit string
	 * @param i2
	 *            second bit string
	 * @return the hamming distance
	 */
	public static int packedHamming(float i1, float i2) {
		final int i1l = Float.floatToIntBits(i1);
		final int i2l = Float.floatToIntBits(i2);

		return packedHamming(i1l, i2l);
	}

	/**
	 * Bitwise (assuming packed bit strings) hamming distance
	 * 
	 * @param i1
	 *            first bit string
	 * @param i2
	 *            second bit string
	 * @return the hamming distance
	 */
	public static int packedHamming(long i1, long i2) {
		int h = 0;
		for (int i = 0; i < LONG_BYTES; i++) {
			final byte b1 = (byte) (i1 & 0xff);
			i1 >>= 8;

			final byte b2 = (byte) (i2 & 0xff);
			i2 >>= 8;

			h += packedHamming(b1, b2);
		}
		return h;
	}

	/**
	 * Bitwise (assuming packed bit strings) hamming distance
	 * 
	 * @param i1
	 *            first bit string
	 * @param i2
	 *            second bit string
	 * @return the hamming distance
	 */
	public static int packedHamming(int i1, int i2) {
		int h = 0;
		for (int i = 0; i < INT_BYTES; i++) {
			final byte b1 = (byte) (i1 & 0xff);
			i1 >>= 8;

			final byte b2 = (byte) (i2 & 0xff);
			i2 >>= 8;

			h += packedHamming(b1, b2);
		}
		return h;
	}

	/**
	 * Bitwise (assuming packed bit strings) hamming distance
	 * 
	 * @param i1
	 *            first bit string
	 * @param i2
	 *            second bit string
	 * @return the hamming distance
	 */
	public static int packedHamming(byte i1, byte i2) {
		return BYTE_BIT_COUNTS[(i1 ^ i2) & 0xFF];
	}

	/**
	 * Bitwise (assuming packed bit strings) hamming distance
	 * 
	 * @param i1
	 *            first bit string
	 * @param i2
	 *            second bit string
	 * @return the hamming distance
	 */
	public static int packedHamming(char i1, char i2) {
		int h = 0;
		for (int i = 0; i < CHAR_BYTES; i++) {
			final byte b1 = (byte) (i1 & 0xff);
			i1 >>= 8;

			final byte b2 = (byte) (i2 & 0xff);
			i2 >>= 8;

			h += packedHamming(b1, b2);
		}
		return h;
	}

	/**
	 * Bitwise (assuming packed bit strings) hamming distance
	 * 
	 * @param i1
	 *            first bit string
	 * @param i2
	 *            second bit string
	 * @return the hamming distance
	 */
	public static int packedHamming(short i1, short i2) {
		int h = 0;
		for (int i = 0; i < SHORT_BYTES; i++) {
			final byte b1 = (byte) (i1 & 0xff);
			i1 >>= 8;

			final byte b2 = (byte) (i2 & 0xff);
			i2 >>= 8;

			h += packedHamming(b1, b2);
		}
		return h;
	}

	/**
	 * Unpack a binary string ("10011...") into a double
	 * 
	 * @param bits
	 * @return a double value with the same bit pattern defined by bits
	 */
	public static double unpackDouble(String bits) {
		return Double.longBitsToDouble(Long.parseLong(bits, 2));
	}

	/**
	 * Unpack a binary string ("10011...") into a float
	 * 
	 * @param bits
	 * @return a float value with the same bit pattern defined by bits
	 */
	public static float unpackFloat(String bits) {
		return Float.intBitsToFloat(Integer.parseInt(bits, 2));
	}

	/**
	 * Unpack a binary string ("10011...") into an int
	 * 
	 * @param bits
	 * @return an int value with the same bit pattern defined by bits
	 */
	public static int unpackInt(String bits) {
		return Integer.parseInt(bits, 2);
	}

	/**
	 * Unpack a binary string ("10011...") into a long
	 * 
	 * @param bits
	 * @return a long value with the same bit pattern defined by bits
	 */
	public static long unpackLong(String bits) {
		return Long.parseLong(bits, 2);
	}

	/**
	 * Unpack a binary string ("10011...") into a short
	 * 
	 * @param bits
	 * @return a short value with the same bit pattern defined by bits
	 */
	public static short unpackShort(String bits) {
		return Short.parseShort(bits, 2);
	}

	/**
	 * Unpack a binary string ("10011...") into a byte
	 * 
	 * @param bits
	 * @return a byte value with the same bit pattern defined by bits
	 */
	public static byte unpackByte(String bits) {
		return Byte.parseByte(bits, 2);
	}
}
