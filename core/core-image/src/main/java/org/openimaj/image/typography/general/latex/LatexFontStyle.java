package org.openimaj.image.typography.general.latex;

import java.text.AttributedCharacterIterator.Attribute;
import java.util.Map;

import org.openimaj.image.renderer.ImageRenderer;
import org.openimaj.image.typography.FontStyle;

/**
 *
 * @param <T>
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class LatexFontStyle<T> extends FontStyle<LatexFont,T>{

	public static final Attribute TEXT_MODE = new FontStyleAttribute("TEXT_MODE");
	boolean textMode;

	/**
	 * @param font
	 * @param renderer
	 */
	public LatexFontStyle(LatexFont font, ImageRenderer<T, ?> renderer) {
		super(font, renderer);
	}

	/**
	 * In text mode all text is surrounded by a latex \text{} 
	 * @param b
	 */
	public void setTextMode(boolean b) {
		this.textMode = b;
	}
	
	@Override
	public void parseAttributes(Map<? extends Attribute, Object> attrs) {
		super.parseAttributes(attrs);
		if (attrs.containsKey(LatexFontStyle.TEXT_MODE)) this.textMode = ((Boolean) attrs.get(TEXT_MODE)).booleanValue();
		
	}

}
