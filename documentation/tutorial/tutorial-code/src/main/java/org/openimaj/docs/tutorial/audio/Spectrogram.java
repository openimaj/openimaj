/**
 *
 */
package org.openimaj.docs.tutorial.audio;

import java.io.File;

import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.filters.HanningAudioProcessor;
import org.openimaj.video.xuggle.XuggleAudio;
import org.openimaj.vis.audio.AudioSpectrogram;

/**
 *
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
	 *	@throws InterruptedException probably never thrown
	 */
	public static void main( final String[] args ) throws InterruptedException
	{
		// Construct a new audio waveform visualisation
		final AudioSpectrogram aw = new AudioSpectrogram( 440,600 );
		aw.showWindow( "Waveform" );

		// Start a sound grabber that will grab from your default microphone
//		final JavaSoundAudioGrabber jsag = new JavaSoundAudioGrabber( new AudioFormat( 16, 44.1, 1 ) );
//		new Thread( jsag ).start();
		final XuggleAudio xa = new XuggleAudio( new File("/home/dd/Downloads/audiocheck.net_sweep20-20klin.wav"));
		final HanningAudioProcessor jsag = new HanningAudioProcessor( xa, 1024 );

		// Wait until the grabber has started (sometimes it takes a while)
//		while( jsag.isStopped() ) Thread.sleep( 50 );

		// Then send each of the frames to the visualisation
		SampleChunk sc = null;
		while( (sc = jsag.nextSampleChunk()) != null )
			aw.setData( sc );
	}
}
