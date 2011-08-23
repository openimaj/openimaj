package org.openimaj.image.annotation;

import org.openimaj.image.MBFImage;

public interface ImageFeatureProvider<T> {
	public MBFImage getImage();
	public T getFeature();
}
