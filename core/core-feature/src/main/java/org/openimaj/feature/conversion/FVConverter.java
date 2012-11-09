package org.openimaj.feature.conversion;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.FloatFV;
import org.openimaj.util.array.ArrayUtils;

/**
 * Class to convert between common {@link FeatureVector} types.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class FVConverter {
	/**
	 * Convert a {@link DoubleFV} to a {@link FloatFV}.
	 * 
	 * @param fv
	 *            the {@link DoubleFV}
	 * @return the {@link FloatFV}
	 */
	public static FloatFV toFloatFV(DoubleFV fv) {
		return new FloatFV(ArrayUtils.doubleToFloat(fv.values));
	}

	/**
	 * Convert a {@link FeatureVector} to a {@link FloatFV} by first converting
	 * to a {@link DoubleFV}.
	 * 
	 * @param fv
	 *            the {@link DoubleFV}
	 * @return the {@link FloatFV}
	 */
	public static FloatFV toFloatFV(FeatureVector fv) {
		return toFloatFV(fv.asDoubleFV());
	}
}
