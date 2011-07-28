package org.openimaj.image.typography;

import java.text.AttributedCharacterIterator.Attribute;
import java.text.AttributedString;
import java.util.Map;

import org.openimaj.image.Image;
import org.openimaj.image.typography.hershey.HersheyFont;

/**
 * Base class for the representation of font styles.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <F> the {@link Font} type
 * @param <T> the pixel type
 */
public class FontStyle<F extends Font<F>, T> {
	/**
	 * Attributes for styling {@link AttributedString}s.
	 *  
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
	 */
	public static class FontStyleAttribute extends Attribute {
		private static final long serialVersionUID = 1L;
		
		protected FontStyleAttribute(String name) {
			super(name);
		}
	}
	
	/**
	 * Horizontal alignment options
	 * 
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
	 *
	 */
	public static enum HorizontalAlignment {
		HORIZONTAL_CENTER,
		HORIZONTAL_LEFT,
		HORIZONTAL_RIGHT,
	}

	/**
	 * Vertical alignment options
	 *  
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
	 *
	 */
	public static enum VerticalAlignment {
		VERTICAL_TOP,
		VERTICAL_HALF,
		VERTICAL_CAP,
		VERTICAL_BOTTOM,		
	}
	
	/**
	 * Attribute for the font. Value should be a {@link font}.
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
	 * Attribute for the stroke width. Value should be a number in pixels.
	 */
	public static final Attribute LINE_WIDTH = new FontStyleAttribute("lineWidth");
	
	/**
	 * Attribute for stroke colour. Value should be of type <T>.
	 */
	public static final Attribute COLOUR = new FontStyleAttribute("colour");
	
	/**
	 * Attribute for width scaling. Value should be a Number.
	 */
	public static final Attribute WIDTH_SCALE = new FontStyleAttribute("widthScale");
	
	/**
	 * Attribute for height scaling. Value should be a Number.
	 */
	public static final Attribute HEIGHT_SCALE = new FontStyleAttribute("heightScale");
	
	/**
	 * Attribute for horizontal alignment. Must be an instance of {@link HorizontalAlignment}
	 */
	public static final Attribute HORIZONTAL_ALIGNMENT = new FontStyleAttribute("horizontalAlignment");
	
	/**
	 * Attribute for vertical alignment. Must be an instance of {@link VerticalAlignment}
	 */
	public static final Attribute VERTICAL_ALIGNMENT = new FontStyleAttribute("verticalAlignment");

	private static final Font<?> DEFAULT_FONT = HersheyFont.ROMAN_SIMPLEX;
	
	/**
	 * The font
	 */
	public F font;
	
	/**
	 * should the associated text be rendered in italic? 
	 */
	public boolean italic;
	
	/**
	 * Angle in radians for drawing the associated text
	 */
	public float angle;
	
	/**
	 * Stroke width for drawing the associated text 
	 */
	public int lineWidth = 1;
	
	/**
	 * Stroke colour for drawing the associated text 
	 */
	public T colour;
	
	/**
	 * Scaling in the width direction 
	 */
	public float widthScale = 1;
	
	/**
	 * Scaling in the height direction 
	 */
	public float heightScale = 1;
	
	/**
	 * horizontal alignment of the text
	 */
	public HorizontalAlignment horizontalAlignment = HorizontalAlignment.HORIZONTAL_LEFT;
	
	/**
	 * vertical alignment of the text
	 */
	public VerticalAlignment verticalAlignment = VerticalAlignment.VERTICAL_BOTTOM;
	
	protected FontStyle(F font, Image<T, ?> image) {
		colour = image.defaultForegroundColour();
		this.font = font;
	}

	/**
	 * Parse the attributes map and set this FontStyle accordingly.
	 * Subclasses should override this method to add extra attributes.
	 * 
	 * @param attrs the attribute map
	 */
	@SuppressWarnings("unchecked")
	public void parseAttributes(Map<? extends Attribute,Object> attrs) {
		if (attrs.containsKey(FONT)) font = (F) attrs.get(FONT);
		if (attrs.containsKey(ITALIC)) italic = (Boolean) attrs.get(ITALIC);
		if (attrs.containsKey(ANGLE)) angle = ((Number) attrs.get(ANGLE)).floatValue();
		if (attrs.containsKey(LINE_WIDTH)) lineWidth = ((Number) attrs.get(LINE_WIDTH)).intValue();
		if (attrs.containsKey(COLOUR)) colour = (T) attrs.get(COLOUR);
		if (attrs.containsKey(WIDTH_SCALE)) widthScale = ((Number) attrs.get(WIDTH_SCALE)).floatValue();
		if (attrs.containsKey(HEIGHT_SCALE)) heightScale = ((Number) attrs.get(HEIGHT_SCALE)).floatValue();
		if (attrs.containsKey(HORIZONTAL_ALIGNMENT)) horizontalAlignment = (HorizontalAlignment) attrs.get(HORIZONTAL_ALIGNMENT);
		if (attrs.containsKey(VERTICAL_ALIGNMENT)) verticalAlignment = (VerticalAlignment) attrs.get(VERTICAL_ALIGNMENT);		
	}
	
	public FontRenderer<T, FontStyle<F, T>> getRenderer(Image<T, ?> image) {
		return font.getRenderer(image);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> FontStyle parseAttributes(Map<? extends Attribute,Object> attrs, Image<T,?> image) {
		Font<?> fnt = (Font<?>) attrs.get(FONT);
		
		if (fnt == null)
			fnt = DEFAULT_FONT;
		
		FontStyle sty = fnt.createStyle(image);
		sty.parseAttributes(attrs);
		return sty;
	}
}
