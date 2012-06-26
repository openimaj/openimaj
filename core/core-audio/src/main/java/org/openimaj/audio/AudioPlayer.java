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
package org.openimaj.audio;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.openimaj.audio.timecode.AudioTimecode;
import org.openimaj.audio.util.AudioUtils;
import org.openimaj.time.TimeKeeper;
import org.openimaj.time.Timecode;

/**
 *	Wraps the Java Sound APIs into the OpenIMAJ audio core for playing sounds.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 8 Jun 2011
 *	
 */
public class AudioPlayer implements Runnable, TimeKeeper<Timecode>
{
	/** The audio stream being played */
	private AudioStream stream = null;
	
	/** The java audio output stream line */
	private SourceDataLine mLine = null;
	
	/** The current timecode being played */
	private Timecode currentTimecode = null;
	
	/** The device name on which to play */
	private String deviceName = null;
	
	/** The mode of the player */
	private Mode mode = Mode.PLAY;
	
	/**
	 * 	Enumerator for the current state of the audio player.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *	
	 *	@created 29 Nov 2011
	 */
	public enum Mode
	{
		/** The audio player is playing */
		PLAY,
		
		/** The audio player is stopped */
		STOP
	}
	
	/**
	 * 	Default constructor that takes an audio
	 * 	stream to play.
	 * 
	 *	@param a The audio stream to play
	 */
	public AudioPlayer( AudioStream a )
	{
		this( a, null );
	}
	
	/**
	 * 	Play the given stream to a specific device.
	 *	@param a The audio stream to play.
	 *	@param deviceName The device to play the audio to. 
	 */
	public AudioPlayer( AudioStream a, String deviceName )
	{
		this.stream = a;
		this.deviceName = deviceName;
		this.setTimecodeObject( new AudioTimecode(0) );
		
	}
	
	/**
	 * 	Set the timecode object that is updated as the audio is played.
	 *  @param t The timecode object.
	 */
	public void setTimecodeObject( Timecode t )
	{
		this.currentTimecode = t;
	}
	
	/**
	 * 	Returns the current timecode.
	 * 	@return The timecode object.
	 */
	public Timecode getTimecodeObject()
	{
		return this.currentTimecode;
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		try
		{
			// Open the sound system.
			openJavaSound();
		
			if( mode == Mode.PLAY )
			{
				// Read samples until there are no more.
				SampleChunk samples = null;
				while( (samples = stream.nextSampleChunk()) != null )
				{
					// If we have a timecode object to update, we'll update it here
					if( this.currentTimecode != null )
						this.currentTimecode.setTimecodeInMilliseconds( 
							samples.getStartTimecode().getTimecodeInMilliseconds() );	
					
					// Play the samples
					playJavaSound( samples );
				}
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			// Close the sound system
			closeJavaSound();			
		}
	}
	
	/**
	 * 	Create a new audio player in a separate thread that will
	 * 	start playing the audio.
	 * 
	 *	@param as The audio stream to play.
	 *	@return The audio player created.
	 */
	public static AudioPlayer createAudioPlayer( AudioStream as )
	{
		AudioPlayer ap = new AudioPlayer( as );
		new Thread( ap ).start();
		return ap;
	}
	
	/**
	 * 	Open a line to the Java Sound APIs.
	 * 
	 *	@throws Exception if the Java sound system could not be initialised.
	 */
	private void openJavaSound() throws Exception
	{
		try
		{
			// Get a line (either the one we ask for, or any one).
			if( deviceName != null )
					mLine = AudioUtils.getJavaOutputLine( deviceName, this.stream.getFormat() );
			else	mLine = AudioUtils.getAnyJavaOutputLine( this.stream.getFormat() );

			if( mLine == null )
				throw new Exception( "Cannot instantiate a sound line." );
			
			System.out.println( "Creating Java Sound Line with "+
					this.stream.getFormat()+" ("+mLine+")" );

			// If no exception has been thrown we open the line.
			mLine.open();

			// If we've opened the line, we start it running
			mLine.start();
		}
		catch( LineUnavailableException e )
		{
			throw new Exception( "Could not open Java Sound audio line for" +
					" the audio format "+stream.getFormat() );
		}
	}

	/**
	 * 	Play the given sample chunk to the Java sound line. The line should be
	 * 	set up to accept the samples that we're going to give it, as we did
	 * 	that in the {@link #openJavaSound()} method.
	 * 
	 *	@param chunk The chunk to play.
	 */
	private void playJavaSound( SampleChunk chunk )
	{
		byte[] rawBytes = chunk.getSamples();
//		System.out.println( Arrays.toString( rawBytes ) );
		mLine.write( rawBytes, 0, rawBytes.length );
	}

	/**
	 * 	Close down the Java sound APIs.
	 */
	private void closeJavaSound()
	{
		if( mLine != null )
		{
			// Wait for the buffer to empty...
			mLine.drain();
			
			// ...then close
			mLine.close();
			mLine = null;
		}
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.time.TimeKeeper#getTime()
	 */
	@Override
	public Timecode getTime()
	{
		return this.currentTimecode;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.time.TimeKeeper#stop()
	 */
	@Override
	public void stop()
	{
		setMode( Mode.STOP );
	}

	/**
	 * 	Set the mode of the player.
	 *	@param m
	 */
	public void setMode( Mode m )
	{
		this.mode = m;
	}
}
