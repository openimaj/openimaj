/**
 *
 */
package org.openimaj.demos.sandbox.audio;

import java.io.InputStream;

import org.openimaj.audio.AudioPlayer;
import org.openimaj.video.xuggle.XuggleAudio;

/**
 *	Small test that plays an audio file from a stream.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 7 May 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class AudioFromStream
{
	/**
	 *	Main method.
	 *	@param args
	 */
	public static void main( final String[] args )
	{
		final InputStream is = AudioFromStream.class.getResourceAsStream(
				"/org/openimaj/demos/sandbox/audio/WelcomeToTheNews.wav" );

		if( is != null )
		{
			final XuggleAudio xa = new XuggleAudio( is );
			final AudioPlayer ap = AudioPlayer.createAudioPlayer( xa );
			ap.run();
		}
		else
			System.out.println( "Stream not found." );
	}
}
