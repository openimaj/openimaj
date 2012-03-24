package org.openimaj.image.annotation;

import java.util.Collection;

import org.openimaj.image.Image;

public interface AnnotatedImage<I extends Image<?, I>, A> {
	I getImage();
	Collection<A> getAnnotations();
}
