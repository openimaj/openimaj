package org.openimaj.ml.annotation;

/**
 * Interface for objects capable of extracting features from a
 * given object. It is expected that implementors of this
 * interface are threadsafe and can allow multiple calls
 * to {@link #extractFeature(Object)} at the same time (or
 * they at least synchronise the method).
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <F> Type of feature
 * @param <O> Type of object
 */
public interface FeatureExtractor<F, O> {
	/**
	 * Extract features from an object and return them.
	 * @param object the object to extract from
	 * @return the extracted feature
	 */
	F extractFeature(O object);
}
