/**
 *
 */
package org.openimaj.docs.tutorial.fund.audio;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.JavaSoundAudioGrabber;
import org.openimaj.audio.SampleChunk;
import org.openimaj.vis.audio.AudioSpectrogram;

/**
 *	This is the code for exercise 1 in the basic audio tutorial.
 *	When you talk or sing into the computer can you see the pitches in your voice?
 *	How does speech compare to other sounds?
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 19 Jun 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class Spectrogram
{
	/**
	 * 	Main method
	 *	@param args command-line args (not used)
	 * 	@throws InterruptedException
	 */
	public static void main( final String[] args ) throws InterruptedException
	{
		// Construct a new audio waveform visualisation
		final AudioSpectrogram aw = new AudioSpectrogram( 440,600 );
		aw.showWindow( "Spectrogram" );

		// Start a sound grabber that will grab from your default microphone
		final JavaSoundAudioGrabber jsag = new JavaSoundAudioGrabber( new AudioFormat( 16, 44.1, 1 ) );
		new Thread( jsag ).start();

		// Wait until the grabber has started (sometimes it takes a while)
		while( jsag.isStopped() ) Thread.sleep( 50 );

		// Then send each of the frames to the visualisation
		SampleChunk sc = null;
		while( (sc = jsag.nextSampleChunk()) != null )
			aw.setData( sc );
	}
}
