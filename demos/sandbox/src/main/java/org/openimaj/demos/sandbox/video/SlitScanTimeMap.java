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
package org.openimaj.demos.sandbox.video;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.processing.effects.GreyscaleSlitScanProcessor;

/**
 *	A slit scan processor where the time map is an FImage
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 31 Jan 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class SlitScanTimeMap
{
	/**
	 *	@param args
	 * @throws IOException
	 */
	public static void main( final String[] args ) throws IOException
    {
//		final FImage map = new FImage( 320, 240 );
//		MatteGenerator.generateMatte( map, MatteType.LINEAR_VERTICAL_GRADIENT, true );
//		MatteGenerator.generateMatte( map, MatteType.RADIAL_GRADIENT, true );
//		MatteGenerator.generateMatte( map, MatteType.ANGLED_LINEAR_GRADIENT, 125/57.3 );
		final FImage map = ImageUtilities.readF( new File("/home/dd/Desktop/concrete.png") );
		DisplayUtilities.display( map );

		final VideoCapture vc = new VideoCapture( 320, 240 );
		final GreyscaleSlitScanProcessor gssp = new GreyscaleSlitScanProcessor( vc, map, 120 );
		VideoDisplay.createVideoDisplay( gssp );
    }
}
