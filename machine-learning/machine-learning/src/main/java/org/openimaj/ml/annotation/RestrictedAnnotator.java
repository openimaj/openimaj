package org.openimaj.ml.annotation;

import java.util.Collection;
import java.util.List;

/**
 * The {@link RestrictedAnnotator} interface describes annotators
 * that can predict annotations based on an external context
 * that restricts what annotations are allowed. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <O> Type of object being annotated
 * @param <A> Type of annotation
 */
public interface RestrictedAnnotator<O, A> {
	/**
	 * Generate annotations  for the given object, restricting
	 * the potential annotations to coming from the given set.
	 * @param object the image
	 * @param restrict the set of allowed annotations
	 * @return generated annotations
	 */
	public abstract List<ScoredAnnotation<A>> annotate(O object, Collection<A> restrict);
}
