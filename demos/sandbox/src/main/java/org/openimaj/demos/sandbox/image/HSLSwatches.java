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
package org.openimaj.demos.sandbox.image;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;

/**
 *	Displays an HSL swatch
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 7 Jun 2013
 */
public class HSLSwatches
{
	/**
	 * 	Main method.
	 *	@param args command-line args (unused)
	 */
	public static void main( final String[] args )
	{
		// Create an image to fill
		final MBFImage swatchS1 = new MBFImage( 800, 800, 3 );

		// Loop over the image
		for( int y = 0; y < swatchS1.getHeight(); y++ ) {
			for( int x = 0; x < swatchS1.getWidth(); x++ ) {
				// Set the HSL coordinates
				final float[] hsl = new float[3];
				hsl[0] = x / (float)swatchS1.getWidth();
				hsl[1] = 1f;
				hsl[2] = 1 - (y / (float)swatchS1.getHeight());

				// Convert to RGB
				final float[] rgb = new float[3];
				Transforms.HSL_TO_RGB( hsl, rgb );

				// Set the pixel in the RGB image
				swatchS1.setPixel( x, y, new Float[]{ rgb[0], rgb[1], rgb[2] } );
			}
		}

		// Display the image
		DisplayUtilities.display( swatchS1 );
	}
}
