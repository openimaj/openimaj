/**
 *
 */
package org.openimaj.demos.sandbox.image;

import java.io.IOException;
import java.net.MalformedURLException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.mask.MatteGenerator;
import org.openimaj.image.processing.mask.MatteGenerator.MatteType;

/**
 *
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 31 Jan 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class MatteGeneratorTest
{
	/**
	 *	@param args
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static void main( final String args[] ) throws MalformedURLException, IOException
	{
		final FImage linearMatteBlackTop = new FImage( 400, 400 );
		MatteGenerator.generateMatte( linearMatteBlackTop, MatteType.LINEAR_VERTICAL_GRADIENT, true );
		DisplayUtilities.display( linearMatteBlackTop, "Linear Matte - Black Top" );

		final FImage linearMatteWhiteTop = new FImage( 400, 400 );
		MatteGenerator.generateMatte( linearMatteWhiteTop, MatteType.LINEAR_VERTICAL_GRADIENT, false );
		DisplayUtilities.display( linearMatteWhiteTop, "Linear Matte - White Top" );

		final FImage linearMatteBlackLeft = new FImage( 400, 400 );
		MatteGenerator.generateMatte( linearMatteBlackLeft, MatteType.LINEAR_HORIZONTAL_GRADIENT, true );
		DisplayUtilities.display( linearMatteBlackLeft, "Linear Matte - Black Left" );

		final FImage linearMatteWhiteLeft = new FImage( 400, 400 );
		MatteGenerator.generateMatte( linearMatteWhiteLeft, MatteType.LINEAR_HORIZONTAL_GRADIENT, false );
		DisplayUtilities.display( linearMatteWhiteLeft, "Linear Matte - White Left" );

		final FImage radialBlackMiddle = new FImage( 400, 400 );
		MatteGenerator.generateMatte( radialBlackMiddle, MatteType.RADIAL_GRADIENT, false );
		DisplayUtilities.display( radialBlackMiddle, "Radial Matte - Black Middle" );

		FImage radialWhiteMiddle = new FImage( 400, 400 );
		MatteGenerator.generateMatte( radialWhiteMiddle, MatteType.RADIAL_GRADIENT, true );
		DisplayUtilities.display( radialWhiteMiddle, "Radial Matte - White Middle" );

		for( double angle = Math.PI/4; angle < Math.PI*2; angle+= Math.PI/4 )
		{
			final FImage angledGrad = new FImage( 400, 400 );
			MatteGenerator.generateMatte( angledGrad, MatteType.ANGLED_LINEAR_GRADIENT, angle, 200d, 200d );
			DisplayUtilities.display( angledGrad, "Angled Gradient - "+Math.floor(angle*57.3)+" degrees" );
		}

		// This test should show using the matte generator used to generate image alpha mattes

		// Loads an image, makes a copy and inverts the copy adding an alpha matte to it.
		// It then plonks the matted image back into the original image, so there should end
		// up with an image where the inverted version in the centre fades out to the original version.

		// Load an image
		final String testImage = "/org/openimaj/image/image_for_testing.jpg";
		final MBFImage i = ImageUtilities.readMBFAlpha( 
				MatteGeneratorTest.class.getResource( testImage ) );

		// Clone and invert the image, add an alpha matte using a radial gradient
		final MBFImage f = i.clone().inverse();
		radialWhiteMiddle = new FImage( i.getWidth(), i.getHeight() );
		MatteGenerator.generateMatte( radialWhiteMiddle, 
				MatteType.RADIAL_GRADIENT, false );
		f.bands.set( 3, radialWhiteMiddle );

		// Draw the matted image onto the original image
		i.drawImage( f, 0, 0 );

		DisplayUtilities.display( i, "Alpha Matte" );
	}
}
