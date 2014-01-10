package org.openimaj.image.typography.general.latex;

import org.openimaj.image.renderer.ImageRenderer;
import org.openimaj.image.typography.Font;
import org.openimaj.image.typography.FontRenderer;
import org.openimaj.image.typography.FontStyle;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class LatexFont implements Font<LatexFont>{

	@SuppressWarnings("unchecked")
	@Override
	public <T, Q extends FontStyle<LatexFont, T>> FontRenderer<T, Q> getRenderer(ImageRenderer<T, ?> renderer) {
		return (FontRenderer<T, Q>) new LatexFontRenderer<T>();
	}

	@Override
	public <T> LatexFontStyle<T> createStyle(ImageRenderer<T, ?> renderer) {
		return new LatexFontStyle<>(this, renderer);
	}

	@Override
	public String getName() {
		return "latex";
	}

}
