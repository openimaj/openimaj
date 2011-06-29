package org.openimaj.image.processing.face.feature;

import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.io.ReadWriteableBinary;

public interface FacialFeatureFactory<T extends FacialFeature, Q extends DetectedFace> extends ReadWriteableBinary {
	public Class<T> getFeatureClass();
	public T createFeature(Q face, boolean isquery);
}
