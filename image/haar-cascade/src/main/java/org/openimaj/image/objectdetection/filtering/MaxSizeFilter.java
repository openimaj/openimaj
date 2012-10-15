package org.openimaj.image.objectdetection.filtering;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.geometry.shape.Shape;

/**
 * Filter to select the biggest detection. Any detection represented as a
 * {@link Shape} can be filtered.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @param <T>
 * 
 */
public class MaxSizeFilter<T extends Shape> implements DetectionFilter<T, T> {
	@Override
	public List<T> apply(List<T> input) {
		T shape = input.get(0);
		double maxSize = shape.calculateArea();

		for (int i = 1; i < input.size(); i++) {
			final T s = input.get(i);
			final double size = s.calculateArea();

			if (size > maxSize) {
				maxSize = size;
				shape = s;
			}
		}

		final List<T> ret = new ArrayList<T>(1);
		ret.add(shape);
		return ret;
	}
}
