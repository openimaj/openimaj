package org.openimaj.lsh.functions;

/**
 * Helper functions for the hamming distance LSH hashes
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
final class HammingHelper {
	static long convert(double v) {
		return Double.doubleToRawLongBits(v);
	}

	static long convert(long v) {
		return v;
	}

	static int convert(float v) {
		return Float.floatToRawIntBits(v);
	}

	static int convert(int v) {
		return v;
	}

	static short convert(short v) {
		return v;
	}

	static byte convert(byte v) {
		return v;
	}
}
