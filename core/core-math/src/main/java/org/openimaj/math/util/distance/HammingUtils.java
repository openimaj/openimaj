package org.openimaj.math.util.distance;

/**
 * Utilities for hamming distance calculations
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class HammingUtils {
	/**
	 * Bitwise (assuming packed bit strings) hamming distance
	 * @param i1 first bit string
	 * @param i2 second bit string
	 * @return the hamming distance
	 */
	public static int packedHamming(double i1, double i2) {
		long i1l = Double.doubleToRawLongBits(i1);
		long i2l = Double.doubleToRawLongBits(i2);
		
		return packedHamming(i1l, i2l);
	}
	
	/**
	 * Bitwise (assuming packed bit strings) hamming distance
	 * @param i1 first bit string
	 * @param i2 second bit string
	 * @return the hamming distance
	 */
	public static int packedHamming(float i1, float i2) {
		int i1l = Float.floatToIntBits(i1);
		int i2l = Float.floatToIntBits(i2);
		
		return packedHamming(i1l, i2l);
	}
	
	/**
	 * Bitwise (assuming packed bit strings) hamming distance
	 * @param i1 first bit string
	 * @param i2 second bit string
	 * @return the hamming distance
	 */
	public static int packedHamming(long i1, long i2) {
		int d = 0;
		
		for (int j=0; j<Long.SIZE; j++) {
			if (((i1 >>> j) & 1) != ((i2 >>> j) & 1)) {
				d++;
			}
		}
		
		return d;
	}
	
	/**
	 * Bitwise (assuming packed bit strings) hamming distance
	 * @param i1 first bit string
	 * @param i2 second bit string
	 * @return the hamming distance
	 */
	public static int packedHamming(int i1, int i2) {
		int d = 0;
		
		for (int j=0; j<Integer.SIZE; j++) {
			if (((i1 >>> j) & 1) != ((i2 >>> j) & 1)) {
				d++;
			}
		}
		
		return d;
	}
	
	/**
	 * Bitwise (assuming packed bit strings) hamming distance
	 * @param i1 first bit string
	 * @param i2 second bit string
	 * @return the hamming distance
	 */
	public static int packedHamming(byte i1, byte i2) {
		int d = 0;
		
		for (int j=0; j<Byte.SIZE; j++) {
			if (((i1 >>> j) & 1) != ((i2 >>> j) & 1)) {
				d++;
			}
		}
		
		return d;
	}
	
	/**
	 * Bitwise (assuming packed bit strings) hamming distance
	 * @param i1 first bit string
	 * @param i2 second bit string
	 * @return the hamming distance
	 */
	public static int packedHamming(char i1, char i2) {
		int d = 0;
		
		for (int j=0; j<Character.SIZE; j++) {
			if (((i1 >>> j) & 1) != ((i2 >>> j) & 1)) {
				d++;
			}
		}
		
		return d;
	}
	
	/**
	 * Bitwise (assuming packed bit strings) hamming distance
	 * @param i1 first bit string
	 * @param i2 second bit string
	 * @return the hamming distance
	 */
	public static int packedHamming(short i1, short i2) {
		int d = 0;
		
		for (int j=0; j<Short.SIZE; j++) {
			if (((i1 >>> j) & 1) != ((i2 >>> j) & 1)) {
				d++;
			}
		}
		
		return d;
	}
	
	/**
	 * Unpack a binary string ("10011...") into a double
	 * @param bits
	 * @return a double value with the same bit pattern defined by bits
	 */
	public static double unpackDouble(String bits) {
		return Double.longBitsToDouble(Long.parseLong(bits, 2));
	}
	
	/**
	 * Unpack a binary string ("10011...") into a float
	 * @param bits
	 * @return a float value with the same bit pattern defined by bits
	 */
	public static float unpackFloat(String bits) {
		return Float.intBitsToFloat(Integer.parseInt(bits, 2));
	}
	
	/**
	 * Unpack a binary string ("10011...") into an int
	 * @param bits
	 * @return an int value with the same bit pattern defined by bits
	 */
	public static int unpackInt(String bits) {
		return Integer.parseInt(bits, 2);
	}
	
	/**
	 * Unpack a binary string ("10011...") into a long
	 * @param bits
	 * @return a long value with the same bit pattern defined by bits
	 */
	public static long unpackLong(String bits) {
		return Long.parseLong(bits, 2);
	}
	
	/**
	 * Unpack a binary string ("10011...") into a short
	 * @param bits
	 * @return a short value with the same bit pattern defined by bits
	 */
	public static short unpackShort(String bits) {
		return Short.parseShort(bits, 2);
	}
	
	/**
	 * Unpack a binary string ("10011...") into a byte
	 * @param bits
	 * @return a byte value with the same bit pattern defined by bits
	 */
	public static byte unpackByte(String bits) {
		return Byte.parseByte(bits, 2);
	}
}
