/**
 * 
 */
package org.openimaj.demos.audio;

import org.openimaj.audio.AudioPlayer;
import org.openimaj.audio.AudioStream;
import org.openimaj.audio.VolumeAdjustProcessor;
import org.openimaj.demos.Demo;
import org.openimaj.video.xuggle.XuggleAudio;

/**
 *	A basic demonstration that shows playing audio through the OpenIMAJ
 *	APIs. It also demonstrates the application of an audio processor to a stream
 *	which can be played.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created Nov 10, 2011
 *	@version $Author$, $Revision$, $Date$
 */
@Demo(
	author = "David Dupplaw" , 
	description = "Demonstrates playing audio from OpenIMAJ" , 
	keywords = { "audio", "player", "processing" } , 
	title = "Audio Player" 
)
public class AudioPlayerDemo
{
	/** The file we're going to play */
	public final static String AUDIO_FILE = "/org/openimaj/demos/audio/140bpm-Arp.mp3";
	
	/**
	 * 	Default constructor
	 */
	public AudioPlayerDemo()
	{
		XuggleAudio s = new XuggleAudio(
				AudioPlayerDemo.class.getResource( AUDIO_FILE ) );
		
		playNormalSound( s );
//		playProcessedSound( s );
	}
	
	/**
	 * 	Plays a sound through the audio API.
	 */
	private void playNormalSound( AudioStream s )
	{
		AudioPlayer ap = new AudioPlayer( s );
		ap.run();
	}
	
	/**
	 * 	Plays a processed sound through the audio API.
	 */
	private void playProcessedSound( AudioStream s )
	{
		VolumeAdjustProcessor vap = new VolumeAdjustProcessor( 0.4f, s );
		
		AudioPlayer ap = new AudioPlayer( vap );
		ap.run();		
	}
	
	/**
	 * 
	 *	@param args
	 */
	public static void main( String[] args )
	{
		new AudioPlayerDemo();
	}
}
