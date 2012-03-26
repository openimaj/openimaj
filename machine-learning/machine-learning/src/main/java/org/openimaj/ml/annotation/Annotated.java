package org.openimaj.ml.annotation;

import java.util.Collection;

/**
 * An object with annotations.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <O> Type of object.
 * @param <A> Type of annotations
 */
public interface Annotated<O, A> {
	/**
	 * @return the object
	 */
	O getObject();
	
	/**
	 * @return the annotations
	 */
	Collection<A> getAnnotations();
}
