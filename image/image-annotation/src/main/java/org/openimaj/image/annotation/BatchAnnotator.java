package org.openimaj.image.annotation;

import java.util.List;

public interface BatchAnnotator<T> extends Annotator<T> {
	public void train(List<ImageFeatureAnnotationProvider<T>> data);
}
