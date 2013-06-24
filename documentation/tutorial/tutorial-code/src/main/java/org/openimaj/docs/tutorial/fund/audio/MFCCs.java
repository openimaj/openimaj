/**
 *
 */
package org.openimaj.docs.tutorial.fund.audio;

import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.audio.features.MFCC;
import org.openimaj.video.xuggle.XuggleAudio;
import org.openimaj.vis.general.BarVisualisation;

/**
 *	Example that shows the extraction of MFCC features from an audio stream
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
	 * 	@throws MalformedURLException Will not be thrown
	 */
	public static void main( final String[] args ) throws MalformedURLException
	{
		// Open a URL to the sine wave sweep. If you have downloaded
		// this file you should use a new File(<filename>) here.
		final XuggleAudio xa = new XuggleAudio(
			new URL( "http://www.audiocheck.net/download.php?" +
					"filename=Audio/audiocheck.net_sweep20-20klin.wav" ) );

		// Create the Fourier transform processor chained to the audio decoder
		final MFCC mfcc = new MFCC( xa );

		// Create a visualisation to show our FFT and open the window now
		final BarVisualisation bv = new BarVisualisation( 400, 200 );
		bv.showWindow( "MFCCs" );

		// Loop through the sample chunks from the audio capture thread
		// sending each one through the feature extractor and displaying
		// the results in the visualisation.
		while( mfcc.nextSampleChunk() != null )
		{
			final double[][] mfccs = mfcc.getLastCalculatedFeatureWithoutFirst();
			bv.setData( mfccs[0] );
		}
	}
}
