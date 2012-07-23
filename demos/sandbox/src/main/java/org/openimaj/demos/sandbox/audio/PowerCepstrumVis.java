/**
 * 
 */
package org.openimaj.demos.sandbox.audio;

import java.io.File;

import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.analysis.PowerCepstrumTransform;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.video.xuggle.XuggleAudio;

/**
 *	
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 23 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class PowerCepstrumVis
{
	/**
	 * 
	 *	@param as
	 *	@throws Exception
	 */
	public PowerCepstrumVis( AudioStream as ) throws Exception
    {
		FImage img = new FImage( 1000, 600 );

		PowerCepstrumTransform pct = new PowerCepstrumTransform();
		
		SampleChunk sc = null;
		while( (sc = as.nextSampleChunk()) != null )
		{
			pct.process( sc );
			float[][] c = pct.getLastCepstrum();
			for( int i = 0; i < c[0].length; i++ )
				img.setPixel( img.getWidth()-1, i, c[0][i]/50f );
			img.shiftLeftInplace();
			
			DisplayUtilities.displayName( img, "Power Cepstrum" );
		}
    }
	
	/**
	 * 
	 *	@param args
	 *	@throws Exception
	 */
	public static void main( String[] args ) throws Exception
    {
		XuggleAudio as = new XuggleAudio( new File("/home/dd/mel.mp4" ) );
	    new PowerCepstrumVis( as );
    }
}
