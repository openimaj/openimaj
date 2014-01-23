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
package org.openimaj.image.typography.hershey;

import java.text.AttributedCharacterIterator.Attribute;
import java.util.Map;

import org.openimaj.image.renderer.ImageRenderer;
import org.openimaj.image.typography.FontStyle;

/**
 * Style parameters for Hershey vector fonts.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> pixel type of image
 */
public class HersheyFontStyle<T> extends FontStyle<T> {
	/**
	 * Attribute for the stroke width. Value should be a number in pixels.
	 */
	public static final Attribute STROKE_WIDTH = new FontStyleAttribute("strokeWidth");
	
	/**
	 * Attribute for width scaling. Value should be a Number.
	 */
	public static final Attribute WIDTH_SCALE = new FontStyleAttribute("widthScale");
	
	/**
	 * Attribute for height scaling. Value should be a Number.
	 */
	public static final Attribute HEIGHT_SCALE = new FontStyleAttribute("heightScale");
	
	/**
	 * Attribute for the amount of slant for italic text.
	 */
	public static final Attribute ITALIC_SLANT = new FontStyleAttribute("italicSlant");
	
	/**
	 * Stroke width for drawing the associated text 
	 */
	private int strokeWidth = 1;

	/**
	 * Scaling in the width direction 
	 */
	private float widthScale = 1;
	
	/**
	 * Scaling in the height direction 
	 */
	private float heightScale = 1;
	
	/**
	 * Slant for italic text 
	 */
	private float italicSlant = 0.75f;
	
	@Override
	public void parseAttributes(Map<? extends Attribute,Object> attrs) {
		super.parseAttributes(attrs);
		
		if (attrs.containsKey(STROKE_WIDTH)) strokeWidth = ((Number) attrs.get(STROKE_WIDTH)).intValue();
		if (attrs.containsKey(WIDTH_SCALE)) widthScale = ((Number) attrs.get(WIDTH_SCALE)).floatValue();
		if (attrs.containsKey(HEIGHT_SCALE)) heightScale = ((Number) attrs.get(HEIGHT_SCALE)).floatValue();
	}
	
	/**
	 * Construct with the default parameters for the given image type
	 * @param image 
	 */
	protected HersheyFontStyle(HersheyFont font, ImageRenderer<T, ?> image) {
		super(font, image);
	}

	/**
	 * @return the strokeWidth
	 */
	public int getStrokeWidth() {
		return strokeWidth;
	}

	/**
	 * @param strokeWidth the strokeWidth to set
	 */
	public void setStrokeWidth(int strokeWidth) {
		this.strokeWidth = strokeWidth;
	}

	/**
	 * @return the widthScale
	 */
	public float getWidthScale() {
		return widthScale;
	}

	/**
	 * @param widthScale the widthScale to set
	 */
	public void setWidthScale(float widthScale) {
		this.widthScale = widthScale;
	}

	/**
	 * @return the heightScale
	 */
	public float getHeightScale() {
		return heightScale;
	}

	/**
	 * @param heightScale the heightScale to set
	 */
	public void setHeightScale(float heightScale) {
		this.heightScale = heightScale;
	}

	/**
	 * @return the italicSlant
	 */
	public float getItalicSlant() {
		return italicSlant;
	}

	/**
	 * @param italicSlant the italicSlant to set
	 */
	public void setItalicSlant(float italicSlant) {
		this.italicSlant = italicSlant;
	}
	
	/**
	 * Get the actual scale to render the font at. This
	 * is calculated by scaling to the fontSize and then 
	 * applying the widthScale.
	 * @return the actual width scaling
	 */
	public float getActualWidthScale() {
		HersheyFont font = (HersheyFont) this.font; 
		float charHeight = font.data.characterSetMaxY - font.data.characterSetMinY;
		float sizeSF = this.fontSize / charHeight;
		return sizeSF * widthScale;
	}
	
	/**
	 * Get the actual scale to render the font at. This
	 * is calculated by scaling to the fontSize and then 
	 * applying the heightScale.
	 * @return the actual height scaling
	 */
	public float getActualHeightScale() {
		HersheyFont font = (HersheyFont) this.font;
		float charHeight = font.data.characterSetMaxY - font.data.characterSetMinY;
		float sizeSF = this.fontSize / charHeight;
		return sizeSF * heightScale;
	}
}
