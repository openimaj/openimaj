package org.openimaj.image.processing.face.features;

import org.openimaj.image.processing.face.detection.DetectedFace;

public interface FacialFeatureFactory<T extends FacialFeature<T, Q>, Q extends DetectedFace> {
	public T createFeature(Q face, boolean isquery);
}
