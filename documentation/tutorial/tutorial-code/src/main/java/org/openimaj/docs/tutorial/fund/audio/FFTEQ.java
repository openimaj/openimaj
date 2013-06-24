/**
 *
 */
package org.openimaj.docs.tutorial.fund.audio;

import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.audio.analysis.FourierTransform;
import org.openimaj.audio.filters.EQFilter;
import org.openimaj.audio.filters.EQFilter.EQType;
import org.openimaj.video.xuggle.XuggleAudio;
import org.openimaj.vis.general.BarVisualisation;

/**
 * Example that demonstrates chaining of audio processors as well as visualising
 * the output of an FFT operation.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 18 Jun 2013
 * @version $Author$, $Revision$, $Date$
 */
public class FFTEQ
{
	/**
	 * Main method
	 *
	 * @param args Command-line args (unused)
	 * @throws MalformedURLException
	 */
	public static void main( final String[] args ) throws MalformedURLException
	{
		// Open a URL to the sine wave sweep. If you have downloaded
		// this file you should use a new File(<filename>) here.
		final XuggleAudio xa = new XuggleAudio(
			new URL( "http://www.audiocheck.net/download.php?" +
					"filename=Audio/audiocheck.net_sweep20-20klin.wav" ) );

		// Create an EQ processor to pre-process the audio before doing the FFT
		// It will be a low-pass filter with a cut-off at 5KHz
		final EQFilter eq = new EQFilter( xa, EQType.LPF, 5000 );

		// Create the Fourier transform processor chained to the audio decoder
		final FourierTransform fft = new FourierTransform( eq );

		// Create a visualisation to show our FFT and open the window now
		// To avoid the visualisation scaling up the values once the peak
		// fades out, we need to disable auto scaling.
		final BarVisualisation bv = new BarVisualisation( 400, 200 );
		bv.setMaxValue( 1E12 );
		bv.setAutoScale( false );
		bv.showWindow( "FFTs" );

		// Loop through the sample chunks from the audio capture thread
		// sending each one through the feature extractor and displaying
		// the results in the visualisation.
		while( fft.nextSampleChunk() != null )
		{
			final float[][] ffts = fft.getMagnitudes();
			bv.setData( ffts[0] );
		}
	}
}
