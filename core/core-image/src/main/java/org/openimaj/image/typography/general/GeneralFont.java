/**
 * 
 */
package org.openimaj.image.typography.general;

import org.openimaj.image.renderer.ImageRenderer;
import org.openimaj.image.typography.Font;
import org.openimaj.image.typography.FontRenderer;
import org.openimaj.image.typography.FontStyle;

/**
 *	This is a wrapper for the Java AWT Font in the OpenIMAJ system.
 *	To use this font, use something like the following:
 *	<pre>{@code
 *		MBFImage img = new MBFImage( 400, 400, 3 );
 *		GeneralFont f = new GeneralFont( "Times New Roman", Font.PLAIN, 72 );
 *		img.drawText( "Hello World", new Point2dImpl(10,500), 
 *			new GeneralFontStyle( f, r, true ) );
 *	}</pre>
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 18 Aug 2011
 *	@version $Author$, $Revision$, $Date$
 */
public class GeneralFont implements Font<GeneralFont>
{
	private String name;
	private int type;
	private float size;
	
	/**
	 * 	Create a font with the given name, type and size. The name
	 * 	should be the name of the font on your system. The type should
	 * 	be Font.PLAIN, Font.BOLD or other Font attributes, and the size
	 * 	in points (assuming 72 ppp). This is the same as the constructor
	 * 	for the java.awt.Font class.
	 * 
	 *	@param name Name of the font
	 *	@param type Font attributes
	 *	@param size Font size in points
	 */
	public GeneralFont( String name, int type, float size )
	{
		this.name = name;
		this.setType( type );
		this.setSize( size );
	}
	
	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.image.typography.Font#getRenderer(org.openimaj.image.renderer.ImageRenderer)
	 */
	@SuppressWarnings( "unchecked" )
	@Override
	public <T, Q extends FontStyle<GeneralFont, T>> FontRenderer<T, Q> 
		getRenderer( ImageRenderer<T, ?> renderer )
	{
		return (FontRenderer<T, Q>)((Object)new GeneralFontRenderer<T>());
	}

	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.image.typography.Font#createStyle(org.openimaj.image.renderer.ImageRenderer)
	 */
	@Override
	public <T> FontStyle<GeneralFont, T> 
		createStyle( ImageRenderer<T, ?> renderer )
	{
		return new GeneralFontStyle<T>( this, renderer );
	}

	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.image.typography.Font#getName()
	 */
	@Override
	public String getName()
	{
		return name;
	}

	/**
	 * 	Set the type of the font.
	 * 
	 *	@param type the type of the font.
	 */
	public void setType( int type )
	{
		this.type = type;
	}

	/**
	 * 	Get the font attributes
	 *	@return the font attributes
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * 	Set the size of the font to draw.
	 *	@param size the size of the font
	 */
	public void setSize( float size )
	{
		this.size = size;
	}

	/**
	 * 	Get the size of the font being drawn.
	 *	@return the size of the font being drawn
	 */
	public float getSize()
	{
		return size;
	}
}
