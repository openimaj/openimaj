/**
 *
 */
package org.openimaj.docs.tutorial.audio;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.JavaSoundAudioGrabber;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.features.MFCC;
import org.openimaj.vis.general.BarVisualisation;

/**
 *	Example that shows the extraction of features from live audio stream
 *	and displaying the results in a visualisation.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 18 Jun 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class MFCCs
{
	/**
	 * 	Main method
	 *	@param args Command-line args (unused)
	 */
	public static void main( final String[] args )
	{
		// Open a live sound capture and run it in a separate thread.
		final JavaSoundAudioGrabber jsag = new JavaSoundAudioGrabber( new AudioFormat( 16, 44.1, 1 ) );
		new Thread( jsag ).start();

		// Create our feature extractor for MFCCs
		final MFCC mfcc = new MFCC();

		// Create a visualisation to show our MFCCs and open the window now
		final BarVisualisation bv = new BarVisualisation( 400, 200 );
		bv.showWindow( "MFCCs" );

		// Loop through the sample chunks from the audio capture thread
		// sending each one through the feature extractor and displaying
		// the results in the visualisation.
		SampleChunk sa;
		while( (sa = jsag.nextSampleChunk()) != null )
		{
			try
			{
				// Calculate the MFCC features
				mfcc.process( sa );
				final double[][] mfccs = mfcc.getLastCalculatedFeature();

				// Display the features (for the first [and only] channel)
				bv.setData( mfccs[0] );
			}
			catch( final Exception e )
			{
				e.printStackTrace();
			}
		}
	}
}
