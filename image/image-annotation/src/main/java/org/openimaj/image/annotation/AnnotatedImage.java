package org.openimaj.image.annotation;

import java.util.Collection;

import org.openimaj.image.Image;

/**
 * An image with annotations.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <I> Type of image.
 * @param <A> Type of annotations
 */
public interface AnnotatedImage<I extends Image<?, I>, A> {
	/**
	 * @return the image
	 */
	I getImage();
	
	/**
	 * @return the annotations
	 */
	Collection<A> getAnnotations();
}
