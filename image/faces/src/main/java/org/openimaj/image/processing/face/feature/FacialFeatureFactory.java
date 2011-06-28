package org.openimaj.image.processing.face.feature;

import org.openimaj.image.processing.face.detection.DetectedFace;

public interface FacialFeatureFactory<T extends FacialFeature, Q extends DetectedFace> {
	public T createFeature(Q face, boolean isquery);
}
