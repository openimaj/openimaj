/**
 * 
 */
package org.openimaj.demos.sandbox.audio;

import java.io.File;

import javax.swing.JFrame;

import org.openimaj.audio.AudioEventListener;
import org.openimaj.audio.AudioPlayer;
import org.openimaj.audio.SampleChunk;
import org.openimaj.video.xuggle.XuggleAudio;
import org.openimaj.vis.audio.AudioWaveform;

/**
 *	Shows a live waveform of an audio file.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 31 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class AudioWaveformVisTest
{
	/**
	 *	@param args
	 * 	@throws InterruptedException 
	 */
	public static void main( final String[] args ) throws InterruptedException
	{
		String name = "heads1.mpeg";
		if( args.length > 0 )
			name = args[0];
		
		final XuggleAudio xa = new XuggleAudio( new File( name ) );

		final AudioWaveform aw = new AudioWaveform( 1000, 500 );
		aw.setMaximum( Integer.MAX_VALUE );
		final JFrame f = aw.showWindow( "Audio" );
		
		final AudioPlayer ap = new AudioPlayer( xa );
		ap.addAudioEventListener( new AudioEventListener()
		{
			@Override
			public void beforePlay( final SampleChunk sc )
			{
			}
			
			@Override
			public void audioEnded()
			{
				f.dispose();
			}
			
			@Override
			public void afterPlay( final AudioPlayer ap, final SampleChunk sc )
			{
				aw.setData( sc.getSampleBuffer() );
			}
		} );
		ap.run();		
	}
}
