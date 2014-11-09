package org.openimaj.image.text.extraction.swt;

import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Base class for candidate letters, lines and words. Contains a cached bounding
 * box.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
abstract class Candidate {
	/**
	 * The regular, axis-oriented bounds of this candidate.
	 */
	protected Rectangle regularBoundingBox;

	/**
	 * Get the regular (axis-oriented) bounding box around this object
	 * 
	 * @return the bounding box
	 */
	public Rectangle getRegularBoundingBox() {
		return regularBoundingBox;
	}
}
