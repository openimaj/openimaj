package org.openimaj.image.processing.face.features;

import org.openimaj.image.processing.face.parts.DetectedFace;

public interface FacialFeatureFactory<T extends FacialFeature<T>> {
	public T createFeature(DetectedFace face, boolean isquery);
}
