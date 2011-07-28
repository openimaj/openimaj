package org.openimaj.image.typography;

import org.openimaj.image.Image;

public interface Font<F extends Font<F>> {
	public <T, Q extends FontStyle<T>> FontRenderer<T, F, Q> getRenderer(Image<T,?> image);
	public <T, Q extends FontStyle<T>> FontStyle<T> createStyle(Image<T, ?> image);
	public String getName();
}
