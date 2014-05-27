package org.openimaj.image.processing.resize.filters;

import org.openimaj.image.processing.resize.ResizeFilterFunction;

/**
 * Point filter for the resampling function
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 
 */
public class PointFilter extends BoxFilter {
	/**
	 * The singleton instance of the filter
	 */
	public static ResizeFilterFunction INSTANCE = new PointFilter();

	@Override
	public double getSupport() {
		return 0;
	}
}
