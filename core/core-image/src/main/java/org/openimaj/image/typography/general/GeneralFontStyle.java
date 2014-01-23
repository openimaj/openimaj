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
import org.openimaj.image.typography.FontStyle;

/**
 *	This is a wrapper and extensions for the Java AWT Font as an OpenIMAJ
 *	Font style.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 18 Aug 2011
 *	
 *
 *	@param <T> The pixel type
 */
public class GeneralFontStyle<T> extends FontStyle<T>
{
	/** Whether to draw the font as outline or not */
	private boolean outline = false;

	/**
	 * 	Create a new font style that will be drawn solid.
	 * 
	 *	@param font The font to use
	 *	@param renderer The renderer to draw with
	 */
	public GeneralFontStyle( final GeneralFont font, final ImageRenderer<T, ?> renderer )
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
	public GeneralFontStyle( final GeneralFont font, final ImageRenderer<T, ?> renderer,
			final boolean outline )
	{
		super( font, renderer );
		this.outline = outline;
	}

	/**
	 * 	Set whether this font should be drawn in outline or not.
	 *	@param outline Whether to draw in outline or not
	 */
	public void setOutline( final boolean outline )
	{
		this.outline = outline;
	}

	/**
	 * 	Returns whether this font is to be drawn in outline or not
	 *	@return Whether the font will be drawn in outline or not
	 */
	public boolean isOutline()
	{
		return this.outline;
	}
}
