package org.openimaj.image.typography.hershey;

import java.io.IOException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.typography.Font;
import org.openimaj.image.typography.FontRenderer;
import org.openimaj.image.typography.FontStyle;

public enum HersheyFont implements Font<HersheyFont> {
		ROWMANT("rowmant.jhf") {
			@Override
			public String getName() {
				return "ROWMANT";
			}
		}
	;

	private final static HersheyFontRenderer<Object> renderer = new HersheyFontRenderer<Object>();
	
	protected HersheyFontData data;
	
	private HersheyFont(String fntName) {
		try {
			data = new HersheyFontData(fntName);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T, Q extends FontStyle<T>> FontRenderer<T,HersheyFont,Q> getRenderer(Image<T, ?> image) {
		return (FontRenderer<T, HersheyFont, Q>) renderer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, Q extends FontStyle<T>> HersheyFontStyle<T> createStyle(Image<T, ?> image) {
		HersheyFontStyle<T> sty = new HersheyFontStyle<T>();
		
		if (image instanceof FImage) {
			((HersheyFontStyle<Float>)sty).lineColour = 1f;
			((HersheyFontStyle<Float>)sty).fillColour = 1f;
		}
		
		return sty;
	}
	
	public static void main(String[] args) {
		FImage image = new FImage(500, 500);
		FontRenderer<Float,HersheyFont, HersheyFontStyle<Float>> renderer = HersheyFont.ROWMANT.getRenderer(image);
		renderer.renderText(image, "hello world", 30, 200, ROWMANT, ROWMANT.createStyle(image));
		
		DisplayUtilities.display(image);
	}
}
