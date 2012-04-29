package org.openimaj.math.util.distance;

/**
 * Utilities for hamming distance calculations
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
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
}
