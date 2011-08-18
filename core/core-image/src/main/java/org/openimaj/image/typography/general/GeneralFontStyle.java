/**
 * 
 */
package org.openimaj.image.typography.general;

import org.openimaj.image.renderer.ImageRenderer;
import org.openimaj.image.typography.FontStyle;

/**
 *	This is a wrapper and extensions for the Java AWT Font as an OpenIMAJ
 *	Font style.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 18 Aug 2011
 *	@version $Author$, $Revision$, $Date$
 *
 *	@param <T> The pixel type
 */
public class GeneralFontStyle<T> extends FontStyle<GeneralFont,T>
{
	/** Whether to draw the font as outline or not */
	private boolean outline = false;
	
	/**
	 * 	Create a new font style that will be drawn solid.
	 * 
	 *	@param font The font to use
	 *	@param renderer The renderer to draw with
	 */
	public GeneralFontStyle( GeneralFont font, ImageRenderer<T, ?> renderer )
	{
		this( font, renderer, false );
	}

	/**
	 * 	Create a new GeneralFontStyle.
	 * 
	 *	@param font The font to use (must be a {@link GeneralFont})
	 *	@param renderer The renderer to draw with
	 *	@param outline Whether to draw in outline or not
	 */
	public GeneralFontStyle( GeneralFont font, ImageRenderer<T, ?> renderer, 
			boolean outline )
	{
		super( font, renderer );
		this.outline = outline;
	}

	/**
	 * 	Set whether this font should be drawn in outline or not.
	 *	@param outline Whether to draw in outline or not
	 */
	public void setOutline( boolean outline )
	{
		this.outline = outline;
	}

	/**
	 * 	Returns whether this font is to be drawn in outline or not
	 *	@return Whether the font will be drawn in outline or not
	 */
	public boolean isOutline()
	{
		return outline;
	}
}
