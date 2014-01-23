package org.openimaj.vis.ternary;

import java.text.AttributedCharacterIterator.Attribute;
import java.util.HashMap;
import java.util.Map;

import org.openimaj.image.colour.ColourMap;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.typography.FontStyle;
import org.openimaj.image.typography.general.GeneralFont;
import org.openimaj.image.typography.mathml.MathMLFont;

/**
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class TernaryParams extends HashMap<String, Object> {
	/**
	 * 
	 */
	public TernaryParams() {
		this.put(COLOUR_MAP, ColourMap.Greys3);
		this.put(BG_COLOUR, RGBColour.BLACK);
		this.put(PADDING, 0);
		Map<Attribute, Object> fontAttrs = new HashMap<Attribute, Object>();
		fontAttrs.put(FontStyle.FONT, new GeneralFont("Arial", 16));
		fontAttrs.put(FontStyle.FONT_SIZE, 16);
		fontAttrs.put(FontStyle.HORIZONTAL_ALIGNMENT, FontStyle.HorizontalAlignment.HORIZONTAL_CENTER);

		this.put(LABEL_FONT, fontAttrs);
		this.put(TRIANGLE_BORDER, false);
		this.put(TRIANGLE_BORDER_THICKNESS, 3);
		this.put(TRIANGLE_BORDER_TICK_THICKNESS, 2);
		this.put(TRIANGLE_BORDER_TICKS, false);
		this.put(TRIANGLE_BORDER_COLOUR, RGBColour.BLACK);
		this.put(DRAW_SCALE, false);

		fontAttrs = new HashMap<Attribute, Object>();
		fontAttrs.put(FontStyle.FONT, new GeneralFont("Arial", 16));
		fontAttrs.put(FontStyle.FONT_SIZE, 16);
		fontAttrs.put(FontStyle.COLOUR, RGBColour.BLACK);
		fontAttrs.put(FontStyle.HORIZONTAL_ALIGNMENT, FontStyle.HorizontalAlignment.HORIZONTAL_RIGHT);
		fontAttrs.put(FontStyle.VERTICAL_ALIGNMENT, FontStyle.VerticalAlignment.VERTICAL_TOP);
		this.put(SCALE_FONT, fontAttrs);
		this.put(SCALE_MIN, "min");
		this.put(SCALE_MAX, "max");
		fontAttrs = new HashMap<Attribute, Object>();
		fontAttrs.put(FontStyle.FONT, new MathMLFont());
		fontAttrs.put(FontStyle.FONT_SIZE, 12);
		fontAttrs.put(FontStyle.COLOUR, RGBColour.BLACK);
		fontAttrs.put(FontStyle.HORIZONTAL_ALIGNMENT, FontStyle.HorizontalAlignment.HORIZONTAL_CENTER);
		fontAttrs.put(FontStyle.VERTICAL_ALIGNMENT, FontStyle.VerticalAlignment.VERTICAL_TOP);
		this.put(TICK_FONT, fontAttrs);
		this.put(LABEL_BACKGROUND, null);
		this.put(LABEL_BORDER, null);
		this.put(LABEL_PADDING, 0);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -1188907996988444966L;
	/**
	 * 
	 */
	public static final String COLOUR_MAP = "ternary.colour.map";
	/**
	 * 
	 */
	public static final String PADDING = "ternary.border";
	/**
	 * 
	 */
	public static final String LABELS = "ternary.label.data";
	/**
	 * 
	 */
	public static final String LABEL_FONT = "ternary.label.font";
	
	/**
	 * 
	 */
	public static final String LABEL_BACKGROUND = "ternary.label.background";
	
	/**
	 * 
	 */
	public static final String LABEL_BORDER = "ternary.label.border";
	/**
	 * 
	 */
	public static final String LABEL_PADDING = "ternary.label.padding";
	/**
	 * 
	 */
	public static final String BG_COLOUR = "ternary.bg.colour";
	/**
	 * 
	 */
	public static final String TRIANGLE_BORDER = "ternary.triangle_border.on";
	/**
	 * 
	 */
	public static final String TRIANGLE_BORDER_THICKNESS = "ternary.triangle_border.thickness";
	/**
	 * 
	 */
	public static final String TRIANGLE_BORDER_TICKS = "ternary.triangle_border.ticks";
	/**
	 * 
	 */
	public static final String TRIANGLE_BORDER_COLOUR = "ternary.triangle_border.colour";
	/**
	 * 
	 */
	public static final String TRIANGLE_BORDER_TICK_THICKNESS = "ternary.triangle_border.ticks.thickness";
	
	/**
	 * 
	 */
	public static final String TICK_FONT = "ternary.triangle_border.ticks.font";
	/**
	 * 
	 */
	public static final String DRAW_SCALE = "ternary.scale.draw";
	public static final float TOP_RIGHT_X = 0.9f;
	public static final float TOP_RIGHT_Y = 0.0f;
	public static final String SCALE_FONT = "ternary.scale.font";
	public static final String SCALE_MIN = "ternary.scale.min.text";
	public static final String SCALE_MAX = "ternary.scale.max.text";
	
	

	/**
	 * @param param
	 * @return the entry
	 */
	@SuppressWarnings("unchecked")
	public <T> T getTyped(String param) {
		return (T) this.get(param);
	}

}
