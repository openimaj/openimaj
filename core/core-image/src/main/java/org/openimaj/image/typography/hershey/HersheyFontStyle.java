package org.openimaj.image.typography.hershey;

import java.text.AttributedCharacterIterator.Attribute;
import java.util.Map;

import org.openimaj.image.Image;
import org.openimaj.image.typography.FontStyle;

/**
 * Style parameters for Hershey vector fonts.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T> pixel type of image
 */
public class HersheyFontStyle<T> extends FontStyle<HersheyFont, T> {
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
	protected HersheyFontStyle(HersheyFont font, Image<T, ?> image) {
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
		float charHeight = font.data.characterSetMaxY - font.data.characterSetMinY;
		float sizeSF = this.fontSize / charHeight;
		return sizeSF * heightScale;
	}
}
