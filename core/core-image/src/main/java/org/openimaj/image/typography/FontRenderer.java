package org.openimaj.image.typography;

import org.openimaj.image.Image;
import org.openimaj.math.geometry.shape.Rectangle;

public interface FontRenderer<T, F extends Font, Q extends FontStyle<T>> {
	public void renderText(Image<T,?> image, String text, int x, int y, F font, Q style);
	public Rectangle getBounds(String string, Q style);
}
