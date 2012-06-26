/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 * 
 */
package org.openimaj.demos.audio;

import org.openimaj.audio.AudioPlayer;
import org.openimaj.audio.AudioStream;
import org.openimaj.audio.filters.VolumeAdjustProcessor;
import org.openimaj.demos.Demo;
import org.openimaj.video.xuggle.XuggleAudio;

/**
 *	A basic demonstration that shows playing audio through the OpenIMAJ
 *	APIs. It also demonstrates the application of an audio processor to a stream
 *	which can be played.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created Nov 10, 2011
 *	
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
	protected void playNormalSound( AudioStream s )
	{
		AudioPlayer ap = new AudioPlayer( s );
		ap.run();
	}
	
	/**
	 * 	Plays a processed sound through the audio API.
	 */
	protected void playProcessedSound( AudioStream s )
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
