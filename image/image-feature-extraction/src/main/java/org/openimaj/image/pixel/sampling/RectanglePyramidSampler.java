package org.openimaj.image.pixel.sampling;

import java.util.Iterator;

import org.openimaj.math.geometry.shape.Rectangle;

public class RectanglePyramidSampler implements Iterable<Rectangle> {

	@Override
	public Iterator<Rectangle> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Create an iterator over each of the levels in the pyramid. Each level is
	 * represented by a {@link RectangleSampler} that describes the individual
	 * rectangles on a level.
	 * 
	 * @return an iterator over the levels.
	 */
	public Iterator<RectangleSampler> levelIterator() {
		return null;

	}
}
