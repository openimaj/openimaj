package org.openimaj.image.processing.haar;

/**
 * A Rectangle with an associated weight. This is used to represent part of a
 * haar-like feature.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class WeightedRectangle {
	/**
	 * The weight assigned to the rectangle
	 */
	public float weight;

	/**
	 * The height
	 */
	public int height;

	/**
	 * The width
	 */
	public int width;

	/**
	 * The top-left y-ordinate
	 */
	public int y;

	/**
	 * The top-left x-ordinate
	 */
	public int x;

	/**
	 * Construct a {@link WeightedRectangle} with the given parameters.
	 * 
	 * @param x
	 *            x-coordinate of top-left
	 * @param y
	 *            y-coordinate of top-left
	 * @param width
	 *            width
	 * @param height
	 *            height
	 * @param weight
	 *            weight
	 */
	public WeightedRectangle(int x, int y, int width, int height, float weight) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.weight = weight;
	}

	/**
	 * parse a string of the form "x y width height weight" to construct a
	 * WeightedRectangle.
	 * 
	 * @param str
	 *            the string to parse
	 * @return the constructed WeightedRectangle
	 */
	public static WeightedRectangle parse(String str) {
		final String[] parts = str.trim().split(" ");

		final int x = Integer.parseInt(parts[0]);
		final int y = Integer.parseInt(parts[1]);
		final int width = Integer.parseInt(parts[2]);
		final int height = Integer.parseInt(parts[3]);
		final float weight = Float.parseFloat(parts[4]);

		return new WeightedRectangle(x, y, width, height, weight);
	}
}
