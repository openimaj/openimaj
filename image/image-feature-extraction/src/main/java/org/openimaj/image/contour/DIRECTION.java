package org.openimaj.image.contour;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;

/**
 * A pixel direction
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public enum DIRECTION {
	NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST;
	static int[] dirx = new int[] { 0, 1, 1, 1, 0, -1, -1, -1 };
	static int[] diry = new int[] { -1, -1, 0, 1, 1, 1, 0, -1 };
	static DIRECTION[] entry = new DIRECTION[] {
			WEST, WEST, NORTH, NORTH, EAST, EAST, SOUTH, SOUTH
	};
	static DIRECTION[] ccentry = new DIRECTION[] {
		EAST, SOUTH, SOUTH, WEST, WEST, NORTH, NORTH, EAST
};

	/**
	 * @return the direction clockwise from this direction
	 */
	public DIRECTION clockwise() {
		final DIRECTION[] vals = DIRECTION.values();
		final DIRECTION dir = vals[(ordinal() + 1) % vals.length];

		return dir;
	}
	
	/**
	 * @return the direction counterClockwise from this direction
	 */
	public DIRECTION counterClockwise() {
		final DIRECTION[] vals = DIRECTION.values();
		int desired = ordinal() - 1;
		final DIRECTION dir = vals[desired == -1 ? desired + vals.length : desired];
		return dir;
	}

	/**
	 * @param img
	 * @param point
	 * @return whether the pixel point to from this point on this img in this
	 *         direction is active
	 */
	public Pixel active(FImage img, Pixel point) {
		final int ord = ordinal();
		final int yy = point.y + diry[ord];
		final int xx = point.x + dirx[ord];
		if (xx < 0 || xx >= img.width || yy < 0 || yy >= img.height)
			return null;
		final float pix = img.pixels[yy][xx];
		return pix != 0 ? new Pixel(xx,yy) : null;
	}

	/**
	 * @return the direction which this pixel was entered from if it were
	 *         entered clockwise
	 */
	public DIRECTION clockwiseEntryDirection() {
		return entry[ordinal()];
	}
	
	/**
	 * @return the direction which this pixel was entered from if it were
	 *         entered clockwise
	 */
	public DIRECTION counterClockwiseEntryDirection() {
		return ccentry[ordinal()];
	}

	/**
	 * @param from
	 * @param to
	 * @return the DIRECTION from and to adjacent pixels
	 */
	public static DIRECTION fromTo(Pixel from, Pixel to) {
		if (from.equals(to))
			return null;
		if (from.y == to.y) {
			if (from.x < to.x)
				return EAST;
			else
				return WEST;
		}
		else if (from.y < to.y) {
			if (from.x == to.x)
				return SOUTH;
			else if (from.x < to.x)
				return SOUTH_EAST;
			else
				return SOUTH_WEST;
		}
		else {
			if (from.x == to.x)
				return NORTH;
			if (from.x < to.x)
				return NORTH_EAST;
			else
				return NORTH_WEST;
		}
	}

	/**
	 * @param from
	 * @return the pixel in the direction from some point
	 */
	public Pixel pixel(Pixel from) {
		return new Pixel(from.x + dirx[ordinal()], from.y + diry[ordinal()]);
	}

	
}
