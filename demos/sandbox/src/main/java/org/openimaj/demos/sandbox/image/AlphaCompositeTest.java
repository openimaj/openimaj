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
package org.openimaj.demos.sandbox.image;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;

/**
 *	Simple alpha composite test that overlays some images on top of each other.
 *	This was written to check that some consolidation of the alpha compositing
 *	code hadn't affected other parts.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 24 Jul 2013
 */
public class AlphaCompositeTest
{
	/**
	 *
	 *	@param args
	 *	@throws MalformedURLException
	 *	@throws IOException
	 */
	public static void main( final String[] args ) throws MalformedURLException, IOException
	{
		final MBFImage img1 = ImageUtilities.readMBF( new URL("http://www.walkmag.co.uk/wp-content/uploads/2012/05/Wineglass_Bay-landscape.jpg") );
		final MBFImage img2 = ImageUtilities.readMBFAlpha( new URL("http://www.openimaj.org/images/OpenImaj.png") );
		final MBFImage img3 = ImageUtilities.readMBF( new URL("http://sd.keepcalm-o-matic.co.uk/i/keep-calm-and-rule-the-world-19.png") );

		final FImage alpha = img3.clone().threshold( 0.7f ).flatten().inverse().
			multiplyInplace( 0.4f ).inverse().addInplace( 0.6f );
		img3.addBand( alpha );
		img3.colourSpace = ColourSpace.RGBA;
		img2.colourSpace = ColourSpace.RGBA;

		img1.drawImage( img2, 1400, 50 );
		img1.drawImage( img3, 800, 100 );

		DisplayUtilities.display( img1 );
	}
}
