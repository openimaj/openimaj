package org.openimaj.demos.sandbox.tldcpp.detector;

import org.openimaj.math.geometry.shape.Rectangle;

/**
 * A rectangle that knows which scale index it belongs to
 * @author ss
 *
 */
public class ScaleIndexRectangle extends Rectangle{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The scale index 
	 */
	public int scaleIndex;
}
