/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.image.typography;

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import org.openimaj.image.renderer.ImageRenderer;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * The FontRenderer represents an object capable of rendering text with 
 * a given font and style to an image. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> type of image pixels
 * @param <Q> type of {@link FontStyle}
 */
public abstract class FontRenderer<T, Q extends FontStyle<?, T>> {
	/**
	 * Render the given text string to the image starting at (x, y) with the
	 * given style.
	 * 
	 * @param renderer the renderer
	 * @param text the text
	 * @param x the x-ordinate
	 * @param y the y-ordinate
	 * @param style the style
	 */
	public abstract void renderText(ImageRenderer<T,?> renderer, String text, int x, int y, Q style);
	
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
	 * @param renderer the image renderer
	 * @param text the text
	 * @param x the x-ordinate
	 * @param y the y-ordinate
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> void renderText(ImageRenderer<T,?> renderer, AttributedString text, int x, int y) {
		AttributedCharacterIterator iterator = text.getIterator();
		
		while (true) {
			Character c = iterator.current();
			if (c == AttributedCharacterIterator.DONE) break;
			
			FontStyle sty = FontStyle.parseAttributes(iterator.getAttributes(), renderer);
			FontRenderer fontRenderer = sty.getRenderer(renderer);
			
			Rectangle rect = fontRenderer.getBounds(c.toString(), sty);
			fontRenderer.renderText(renderer, c.toString(), x, y, sty);
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
	 * @param imageRenderer the target image renderer
	 * @return the bounding box
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> Rectangle getBounds(AttributedString text, ImageRenderer<T,?> imageRenderer) {
		AttributedCharacterIterator iterator = text.getIterator();
		int x=0, y=0;
		
		while (true) {
			Character c = iterator.current();
			if (c == AttributedCharacterIterator.DONE) break;
			
			FontStyle sty = FontStyle.parseAttributes(iterator.getAttributes(), imageRenderer);
			FontRenderer renderer = sty.getRenderer(imageRenderer);
			
			Rectangle rect = renderer.getBounds(c.toString(), sty);
			x += rect.width;
			y += rect.height;
			
			iterator.next();
		}
		
		return new Rectangle(0,0,x,y);
	}
}
