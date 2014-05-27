package org.openimaj.image.processing.resize.filters;

import org.openimaj.image.processing.resize.ResizeFilterFunction;

/**
 * Catmull-Rom (Catrom) interpolation filter for the resample function
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class CatmullRomFilter implements ResizeFilterFunction {
	/**
	 * The singleton instance of the filter
	 */
	public static ResizeFilterFunction INSTANCE = new CatmullRomFilter();

	@Override
	public final double filter(double t) {
		if (t < 0) {
			t = -t;
		}
		if (t < 1.0) {
			return 0.5 * (2.0 + t * t * (-5.0 + t * 3.0));
		}
		if (t < 2.0) {
			return 0.5 * (4.0 + t * (-8.0 + t * (5.0 - t)));
		}
		return 0.0;
	}

	@Override
	public final double getSupport() {
		return 2.0;
	}
}
