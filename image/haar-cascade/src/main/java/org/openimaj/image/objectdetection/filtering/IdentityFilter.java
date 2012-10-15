package org.openimaj.image.objectdetection.filtering;

import java.util.List;

/**
 * Identity {@link DetectionFilter}; just outputs the input directly.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            Type being filtered.
 */
public final class IdentityFilter<T> implements DetectionFilter<T, T> {
	@Override
	public List<T> apply(List<T> input) {
		return input;
	}
}
