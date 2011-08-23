package org.openimaj.image.annotation;

import java.util.List;

public interface ImageFeatureAnnotationProvider<T> extends ImageFeatureProvider<T> {
	public List<String> getAnnotations();
}
