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
