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

import java.awt.Font;
import java.net.URL;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.typography.general.GeneralFont;

/**
 *	
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 20 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class OverlayTest
{
	/**
	 *	@param args
	 *	@throws Exception
	 */
	public static void main( final String[] args )
			throws Exception
			{
		// ------------------------------------------------------------------
		// FImage test
		// ------------------------------------------------------------------
		final FImage fi1 = ImageUtilities.readF(
				new URL("http://www.sableandhogg-gallery.co.uk/shop/images/autumn%20landscape,%20beacons.jpg") );
		final FImage fi2 = new FImage( 300, 100 );
		fi2.drawText( "HELLO", 0, 35, new GeneralFont( "Arial", Font.BOLD ), 48 );
		final FImage alpha = fi2.clone().multiplyInplace( 0.5f );
		fi1.overlayInplace( fi2, alpha, 200, 200 );
		System.out.println( fi1 );

		DisplayUtilities.display( fi1, "Composite" );

		// ------------------------------------------------------------------
		// MBFImage test
		// ------------------------------------------------------------------
		final MBFImage i1 = ImageUtilities.readMBF(
				new URL("http://www.sableandhogg-gallery.co.uk/shop/images/autumn%20landscape,%20beacons.jpg") );
		final MBFImage i2 = new MBFImage( 300, 100, 3 );
		i2.drawText( "HELLO", 0, 35, new GeneralFont( "Arial", Font.BOLD ), 48 );
		i2.addBand( alpha );
		i1.overlayInplace( i2, 200, 200 );
		System.out.println( i1 );

		DisplayUtilities.display( i1, "Multiband Composite" );


		final MBFImage i1b = ImageUtilities.readMBF(
				new URL("http://www.sableandhogg-gallery.co.uk/shop/images/autumn%20landscape,%20beacons.jpg") );
		i1b.drawImage( i2, 200, 200 );
		DisplayUtilities.display( i1b, "Multiband Composite" );

			}
}
