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
