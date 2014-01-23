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
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 18 Aug 2011
 *	
 */
public class GeneralFont implements Font<GeneralFont>
{
	private final String name;
	private int type;
	//	private float size;

	/**
	 * 	Create a font with the given name, type and size. The name
	 * 	should be the name of the font on your system. The type should
	 * 	be Font.PLAIN, Font.BOLD or other Font attributes.
	 * 	This is the same as the constructor
	 * 	for the java.awt.Font class.
	 * 
	 *	@param name Name of the font
	 *	@param type Font attributes
	 */
	public GeneralFont( final String name, final int type )
	{
		this.name = name;
		this.setType( type );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.typography.Font#getRenderer(org.openimaj.image.renderer.ImageRenderer)
	 */
	@SuppressWarnings( "unchecked" )
	@Override
	public <T, Q extends FontStyle<T>> FontRenderer<T, Q>
	getRenderer( final ImageRenderer<T, ?> renderer )
	{
		return (FontRenderer<T, Q>)(Object)new GeneralFontRenderer<T>();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.typography.Font#createStyle(org.openimaj.image.renderer.ImageRenderer)
	 */
	@Override
	public <T> FontStyle< T>
	createStyle( final ImageRenderer<T, ?> renderer )
	{
		return new GeneralFontStyle<T>( this, renderer );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.typography.Font#getName()
	 */
	@Override
	public String getName()
	{
		return this.name;
	}

	/**
	 * 	Set the type of the font.
	 * 
	 *	@param type the type of the font.
	 */
	public void setType( final int type )
	{
		this.type = type;
	}

	/**
	 * 	Get the font attributes
	 *	@return the font attributes
	 */
	public int getType()
	{
		return this.type;
	}
}
