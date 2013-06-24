/**
 *
 */
package org.openimaj.docs.tutorial.fund.audio;

import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.audio.SampleChunk;
import org.openimaj.video.xuggle.XuggleAudio;
import org.openimaj.vis.audio.AudioWaveform;

/**
 *	An example that demonstrates getting audio from a file (or URL) and
 *	displaying the waveform.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 19 Jun 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class AudioWaveformDemo
{
	/**
	 * 	Main method.
	 *	@param args Command line args (no used)
	 * 	@throws MalformedURLException
	 */
	public static void main( final String[] args ) throws MalformedURLException
	{
		// Construct a new audio waveform visualisation
		final AudioWaveform aw = new AudioWaveform( 400, 400 );
		aw.showWindow( "Waveform" );

		// Open a URL to the sine wave sweep. If you have downloaded
		// this file you should use a new File(<filename>) here.
		final XuggleAudio jsag = new XuggleAudio(
				new URL( "http://www.audiocheck.net/download.php?" +
						"filename=Audio/audiocheck.net_sweep20-20klin.wav" ) );

		// Then send each of the frames to the visualisation
		SampleChunk sc = null;
		while( (sc = jsag.nextSampleChunk()) != null )
			aw.setData( sc.getSampleBuffer() );
	}
}
