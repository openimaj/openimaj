package org.openimaj.image.typography;

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import org.openimaj.image.Image;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * The FontRenderer represents an object capable of rendering text with 
 * a given font and style to an image. 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T> type of image pixels
 * @param <Q> type of {@link FontStyle}
 */
public abstract class FontRenderer<T, Q extends FontStyle<?, T>> {
	/**
	 * Render
	 * 
	 * @param image
	 * @param text
	 * @param x
	 * @param y
	 * @param font
	 * @param style
	 */
	public abstract void renderText(Image<T,?> image, String text, int x, int y, Q style);
	
	public abstract Rectangle getBounds(String string, Q style);
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> void renderText(Image<T,?> image, AttributedString text, int x, int y) {
		AttributedCharacterIterator iterator = text.getIterator();
		
		while (true) {
			Character c = iterator.current();
			if (c == AttributedCharacterIterator.DONE) break;
			
			FontStyle sty = FontStyle.parseAttributes(iterator.getAttributes(), image);
			FontRenderer renderer = sty.getRenderer(image);
			
			Rectangle rect = renderer.getBounds(c.toString(), sty);
			renderer.renderText(image, c.toString(), x, y, sty);
			x += rect.width;
			y += rect.height;
			
			iterator.next();
		}
	}
}
