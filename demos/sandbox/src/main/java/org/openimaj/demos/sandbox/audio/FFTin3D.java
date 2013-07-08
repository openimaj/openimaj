/**
 *
 */
package org.openimaj.demos.sandbox.audio;

import java.net.MalformedURLException;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.JavaSoundAudioGrabber;
import org.openimaj.audio.analysis.FourierTransform;
import org.openimaj.audio.processor.FixedSizeSampleAudioProcessor;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.vis.general.ChronologicalScrollingBarVisualisation3D;

/**
 * Example that demonstrates the processing of audio to extract the FFTin3D magnitudes
 * and then visualise it.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 18 Jun 2013
 * @version $Author$, $Revision$, $Date$
 */
public class FFTin3D
{
	/**
	 * Main method
	 *
	 * @param args Command-line args (unused)
	 * @throws MalformedURLException
	 * @throws InterruptedException
	 */
	public static void main( final String[] args ) throws MalformedURLException, InterruptedException
	{
		// Open a URL to the sine wave sweep. If you have downloaded
		// this file you should use a new File(<filename>) here.
		final JavaSoundAudioGrabber xa = new JavaSoundAudioGrabber( new AudioFormat(16,44.1,1) );
		new Thread(xa).start();
		final FixedSizeSampleAudioProcessor fs = new FixedSizeSampleAudioProcessor( xa, 128 );

		// Create the Fourier transform processor chained to the audio decoder
		final FourierTransform fft = new FourierTransform( fs );

		Thread.sleep( 1000 );

		// Create a visualisation to show our FFTin3D and open the window now
//		final BarVisualisation bv = new BarVisualisation( 400, 200 );
//		bv.showWindow( "FFTs" );
		final ChronologicalScrollingBarVisualisation3D bv =
				new ChronologicalScrollingBarVisualisation3D( 1000, 800, 50, 64 );

		// Loop through the sample chunks from the audio capture thread
		// sending each one through the feature extractor and displaying
		// the results in the visualisation.
		while( fft.nextSampleChunk() != null )
		{
			final float[][] ffts = fft.getMagnitudes();
			bv.setData( ArrayUtils.convertToDouble( ffts[0] ) );
			Thread.sleep( (long)(512/44.1) );
		}
	}
}
