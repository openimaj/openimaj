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
	 * Render the given text string to the image starting at (x, y) with the
	 * given style.
	 * 
	 * @param image the image
	 * @param text the text
	 * @param x the x-ordinate
	 * @param y the y-ordinate
	 * @param style the style
	 */
	public abstract void renderText(Image<T,?> image, String text, int x, int y, Q style);
	
	/**
	 * Calculate the bounding box of the rendered text with the given style. 
	 * @param string the text
	 * @param style the style
	 * @return the bounding box
	 */
	public abstract Rectangle getBounds(String string, Q style);
	
	
	/**
	 * Render the given {@link AttributedString} to the image starting at (x,y).
	 * @param <T> the pixel type of the image
	 * @param image the image
	 * @param text the text
	 * @param x the x-ordinate
	 * @param y the y-ordinate
	 */
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
	
	/**
	 * Calculate the bounding box of the given {@link AttributedString}. The image
	 * will not be modified by this call, but is used to properly configure the styles
	 * for bounds estimation.
	 * 
	 * @param <T> the pixel type
	 * @param text the text
	 * @param image the target image
	 * @return the bounding box
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> Rectangle getBounds(AttributedString text, Image<T,?> image) {
		AttributedCharacterIterator iterator = text.getIterator();
		int x=0, y=0;
		
		while (true) {
			Character c = iterator.current();
			if (c == AttributedCharacterIterator.DONE) break;
			
			FontStyle sty = FontStyle.parseAttributes(iterator.getAttributes(), image);
			FontRenderer renderer = sty.getRenderer(image);
			
			Rectangle rect = renderer.getBounds(c.toString(), sty);
			x += rect.width;
			y += rect.height;
			
			iterator.next();
		}
		
		return new Rectangle(0,0,x,y);
	}
}
