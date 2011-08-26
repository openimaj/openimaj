/**
 * 
 */
package org.openimaj.demos;

import java.io.File;

import org.openimaj.audio.AudioStream;
import org.openimaj.video.xuggle.XuggleAudio;

/**
 *	Audio visualisation demonstration.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 8 Jun 2011
 *	@version $Author$, $Revision$, $Date$
 */
public class AudioVisualisation
{	
	private AudioStream stream = null;
	
	public AudioVisualisation( AudioStream stream )
	{
		this.stream = stream;
	}
	
	/**
	 *	@param args
	 */
	public static void main( String[] args )
	{
		new AudioVisualisation( new XuggleAudio( new File( "/Users/ss/Music/Spotify/Ayreon/01011001/02 Comatose.mp3" ) ) );
	}
}
