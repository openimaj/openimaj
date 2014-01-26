package org.openimaj.image.processing.algorithm;

/**
 * Methods and statically defined templates for defining the support of local
 * image filters.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FilterSupport {

	/**
	 * Offsets for using a 3x3 cross shaped mask to select pixels for computing
	 * median.
	 */
	public static final int[][] CROSS_3x3 = {
			{ 0, -1 },
			{ -1, 0 }, { 0, 0 }, { 1, 0 },
			{ 0, 1 }
	};
	/**
	 * Offsets for using a 3x3 blocked shaped mask to select pixels for
	 * computing median.
	 */
	public static final int[][] BLOCK_3x3 = {
			{ -1, -1 }, { 0, -1 }, { 1, -1 },
			{ -1, 0 }, { 0, 0 }, { 1, 0 },
			{ -1, 1 }, { 0, 1 }, { 1, 1 }
	};

	/**
	 * Create a a rectangular support.
	 * 
	 * @param width
	 *            the width of the support
	 * @param height
	 *            the height of the support
	 * @return the support
	 */
	public static int[][] createBlockSupport(int width, int height) {
		final int[][] indices = new int[width * height][2];

		final int startX = -width / 2;
		final int startY = -height / 2;

		for (int i = 0, y = 0; y < height; y++) {
			for (int x = 0; x < width; x++, i++) {
				indices[i][0] = startX + x;
				indices[i][1] = startY + y;
			}
		}

		return indices;
	}
}
