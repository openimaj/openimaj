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

import java.text.AttributedCharacterIterator.Attribute;
import java.text.AttributedString;
import java.util.Map;

import org.openimaj.image.renderer.ImageRenderer;
import org.openimaj.image.typography.hershey.HersheyFont;

/**
 * Base class for the representation of font styles.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T>
 *            the pixel type
 */
public class FontStyle<T> {
	/**
	 * Attributes for styling {@link AttributedString}s.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static class FontStyleAttribute extends Attribute {
		private static final long serialVersionUID = 1L;

		/**
		 * Default constructor
		 * 
		 * @param name
		 *            the name of the attribute
		 */
		public FontStyleAttribute(final String name) {
			super(name);
		}
	}

	/**
	 * Horizontal alignment options
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	public static enum HorizontalAlignment {
		/**
		 * Centered text from the starting point
		 */
		HORIZONTAL_CENTER,
		/**
		 * Left-aligned text from the starting point
		 */
		HORIZONTAL_LEFT,
		/**
		 * Right-aligned text from the starting point
		 */
		HORIZONTAL_RIGHT,
	}

	/**
	 * Vertical alignment options
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	public static enum VerticalAlignment {
		/**
		 *
		 */
		VERTICAL_TOP,
		/**
		 *
		 */
		VERTICAL_HALF,
		/**
		 *
		 */
		VERTICAL_CAP,
		/**
		 * Bottom of characters aligned
		 */
		VERTICAL_BOTTOM,
	}

	/**
	 * Attribute for the font. Value should be a {@link Font}.
	 */
	public static final Attribute FONT = new FontStyleAttribute("font");

	/**
	 * Attribute for italic text. Value should be Boolean.
	 */
	public static final Attribute ITALIC = new FontStyleAttribute("italic");

	/**
	 * Attribute for rotating the text. Value should be a Number in radians.
	 */
	public static final Attribute ANGLE = new FontStyleAttribute("angle");

	/**
	 * Attribute for stroke colour. Value should be of type <T>.
	 */
	public static final Attribute COLOUR = new FontStyleAttribute("colour");

	/**
	 * Attribute for horizontal alignment. Must be an instance of
	 * {@link HorizontalAlignment}
	 */
	public static final Attribute HORIZONTAL_ALIGNMENT = new FontStyleAttribute("horizontalAlignment");

	/**
	 * Attribute for vertical alignment. Must be an instance of
	 * {@link VerticalAlignment}
	 */
	public static final Attribute VERTICAL_ALIGNMENT = new FontStyleAttribute("verticalAlignment");

	/**
	 * Attribute for font size. Must be a Number in pixels.
	 */
	public static final Attribute FONT_SIZE = new FontStyleAttribute("fontSize");

	private static final Font<?> DEFAULT_FONT = HersheyFont.ROMAN_SIMPLEX;

	/**
	 * The font
	 */
	protected Font<?> font;

	/**
	 * should the associated text be rendered in italic?
	 */
	protected boolean italic;

	/**
	 * Angle in radians for drawing the associated text
	 */
	protected float angle;

	/**
	 * Stroke colour for drawing the associated text
	 */
	protected T colour;

	/**
	 * horizontal alignment of the text
	 */
	protected HorizontalAlignment horizontalAlignment = HorizontalAlignment.HORIZONTAL_LEFT;

	/**
	 * vertical alignment of the text
	 */
	protected VerticalAlignment verticalAlignment = VerticalAlignment.VERTICAL_BOTTOM;

	/**
	 * Font size in pixels
	 */
	protected int fontSize = 24;

	protected FontStyle(final Font<?> font, final ImageRenderer<T, ?> renderer) {
		this.colour = renderer.defaultForegroundColour();
		this.font = font;
	}

	/**
	 * @param font
	 * @param col
	 */
	public FontStyle(final Font<?> font, final T col) {
		this.colour = col;
		this.font = font;
	}

	/**
	 * Parse the attributes map and set this FontStyle accordingly. Subclasses
	 * should override this method to add extra attributes.
	 *
	 * @param attrs
	 *            the attribute map
	 */
	@SuppressWarnings("unchecked")
	public void parseAttributes(final Map<? extends Attribute, Object> attrs) {
		if (attrs.containsKey(FontStyle.FONT))
			this.font = (Font<?>) attrs.get(FontStyle.FONT);
		if (attrs.containsKey(FontStyle.ITALIC))
			this.italic = (Boolean) attrs.get(FontStyle.ITALIC);
		if (attrs.containsKey(FontStyle.ANGLE))
			this.angle = ((Number) attrs.get(FontStyle.ANGLE)).floatValue();
		if (attrs.containsKey(FontStyle.COLOUR))
			this.colour = (T) attrs.get(FontStyle.COLOUR);
		if (attrs.containsKey(FontStyle.HORIZONTAL_ALIGNMENT))
			this.horizontalAlignment = (HorizontalAlignment) attrs.get(FontStyle.HORIZONTAL_ALIGNMENT);
		if (attrs.containsKey(FontStyle.VERTICAL_ALIGNMENT))
			this.verticalAlignment = (VerticalAlignment) attrs.get(FontStyle.VERTICAL_ALIGNMENT);
		if (attrs.containsKey(FontStyle.FONT_SIZE))
			this.fontSize = ((Number) attrs.get(FontStyle.FONT_SIZE)).intValue();
	}

	/**
	 * Get the renderer suitable for rendering text with this style into the
	 * given image.
	 * 
	 * @param renderer
	 *            the image renderer
	 * @return the renderer
	 */
	public FontRenderer<T, FontStyle<T>> getRenderer(final ImageRenderer<T, ?> renderer) {
		return this.font.getRenderer(renderer);
	}

	/**
	 * Construct a new FontStyle from the given attribute map, suitable for
	 * rendering into the given image.
	 * 
	 * @param <T>
	 *            the pixel type.
	 * @param attrs
	 *            the attribute map
	 * @param renderer
	 *            the image renderer
	 * @return the FontStyle
	 */
	public static <T> FontStyle<T> parseAttributes(final Map<? extends Attribute, Object> attrs,
			final ImageRenderer<T, ?> renderer)
	{
		Font<?> fnt = (Font<?>) attrs.get(FontStyle.FONT);

		if (fnt == null)
			fnt = FontStyle.DEFAULT_FONT;

		final FontStyle<T> sty = fnt.createStyle(renderer);
		sty.parseAttributes(attrs);
		return sty;
	}

	/**
	 * @return the font
	 */
	public Font<?> getFont() {
		return this.font;
	}

	/**
	 * @param font
	 *            the font to set
	 */
	public void setFont(final Font<?> font) {
		this.font = font;
	}

	/**
	 * @return the italic
	 */
	public boolean isItalic() {
		return this.italic;
	}

	/**
	 * @param italic
	 *            the italic to set
	 */
	public void setItalic(final boolean italic) {
		this.italic = italic;
	}

	/**
	 * @return the angle
	 */
	public float getAngle() {
		return this.angle;
	}

	/**
	 * @param angle
	 *            the angle to set
	 */
	public void setAngle(final float angle) {
		this.angle = angle;
	}

	/**
	 * @return the colour
	 */
	public T getColour() {
		return this.colour;
	}

	/**
	 * @param colour
	 *            the colour to set
	 */
	public void setColour(final T colour) {
		this.colour = colour;
	}

	/**
	 * @return the horizontalAlignment
	 */
	public HorizontalAlignment getHorizontalAlignment() {
		return this.horizontalAlignment;
	}

	/**
	 * @param horizontalAlignment
	 *            the horizontalAlignment to set
	 */
	public void setHorizontalAlignment(final HorizontalAlignment horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
	}

	/**
	 * @return the verticalAlignment
	 */
	public VerticalAlignment getVerticalAlignment() {
		return this.verticalAlignment;
	}

	/**
	 * @param verticalAlignment
	 *            the verticalAlignment to set
	 */
	public void setVerticalAlignment(final VerticalAlignment verticalAlignment) {
		this.verticalAlignment = verticalAlignment;
	}

	/**
	 * @return the fontSize
	 */
	public int getFontSize() {
		return this.fontSize;
	}

	/**
	 * @param fontSize
	 *            the fontSize to set
	 */
	public void setFontSize(final int fontSize) {
		this.fontSize = fontSize;
	}
}
