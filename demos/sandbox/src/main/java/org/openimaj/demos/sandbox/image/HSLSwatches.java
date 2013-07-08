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
