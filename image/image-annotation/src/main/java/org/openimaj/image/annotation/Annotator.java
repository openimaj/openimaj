package org.openimaj.image.annotation;

import java.util.List;

public abstract interface Annotator<T> {
	public List<AutoAnnotation> annotate(ImageFeatureProvider<T> provider);
}
