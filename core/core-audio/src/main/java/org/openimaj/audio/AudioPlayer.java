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

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.openimaj.audio.timecode.AudioTimecode;
import org.openimaj.audio.util.AudioUtils;
import org.openimaj.time.TimeKeeper;
import org.openimaj.time.Timecode;

/**
 *	Wraps the Java Sound APIs into the OpenIMAJ audio core for playing sounds.
 *	<p>
 *	The {@link AudioPlayer} supports the {@link TimeKeeper} interface so that
 *	other methods can synchronise to the audio timestamps.
 *	<p>
 *	The Audio Player as a {@link TimeKeeper} supports seeking but it may be
 *	possible that the underlying stream does not support seeking so the seek
 *	method may not affect the time keeper as expected.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 8 Jun 2011
 *	
 */
public class AudioPlayer implements Runnable, TimeKeeper<AudioTimecode>
{
	/** The audio stream being played */
	private AudioStream stream = null;
	
	/** The java audio output stream line */
	private SourceDataLine mLine = null;
	
	/** The current timecode being played */
	private AudioTimecode currentTimecode = null;
	
	/** The current audio timestamp */
	private long currentTimestamp = 0;
	
	/** At what timestamp the current timecode was read at */
	private long timecodeReadAt = 0; 
	
	/** The device name on which to play */
	private String deviceName = null;
	
	/** The mode of the player */
	private Mode mode = Mode.PLAY;
	
	/** Listeners for events */
	private final List<AudioEventListener> listeners = new ArrayList<AudioEventListener>();
	
	/** Whether the system has been started */
	private boolean started = false;

	/** 
	 * 	Number of milliseconds in the sound line buffer. < 100ms is good 
	 * 	for real-time whereas the bigger the better for smooth sound reproduction
	 */
	private double soundLineBufferSize = 100;
	
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
		
		/** The audio player is paused */
		PAUSE,
		
		/** The audio player is stopped */
		STOP
	}
	
	/**
	 * 	Default constructor that takes an audio
	 * 	stream to play.
	 * 
	 *	@param a The audio stream to play
	 */
	public AudioPlayer( final AudioStream a )
	{
		this( a, null );
	}
	
	/**
	 * 	Play the given stream to a specific device.
	 *	@param a The audio stream to play.
	 *	@param deviceName The device to play the audio to. 
	 */
	public AudioPlayer( final AudioStream a, final String deviceName )
	{
		this.stream = a;
		this.deviceName = deviceName;		
		this.setTimecodeObject( new AudioTimecode(0) );
	}
	
	/**
	 * 	Set the length of the sound line's buffer in milliseconds. The longer
	 * 	the buffer the less likely the soundline will be to pop but the shorter
	 * 	the buffer the closer to real-time the sound output will be. This value
	 * 	must be set before the audio line is opened otherwise it will have no
	 * 	effect.
	 *	@param ms The length of the sound line in milliseconds.
	 */
	public void setSoundLineBufferSize( final double ms )
	{
		this.soundLineBufferSize = ms;
	}
	
	/**
	 * 	Add the given audio event listener to this player.
	 *	@param l The listener to add.
	 */
	public void addAudioEventListener( final AudioEventListener l )
	{
		this.listeners.add( l );
	}
	
	/**
	 * 	Remove the given event from the listeners on this player.
	 *	@param l The listener to remove.
	 */
	public void removeAudioEventListener( final AudioEventListener l )
	{
		this.listeners.remove( l );
	}

	/**
	 * 	Fires the audio ended event to the listeners.
	 *	@param as The audio stream that ended
	 */
	protected void fireAudioEnded( final AudioStream as )
	{
		for( final AudioEventListener ael : this.listeners )
			ael.audioEnded();
	}
	
	/**
	 * 	Fires an event that says the samples will be played.
	 *	@param sc The samples to play
	 */
	protected void fireBeforePlay( final SampleChunk sc )
	{
		for( final AudioEventListener ael: this.listeners )
			ael.beforePlay( sc );
	}
	
	/**
	 * 	Fires an event that says the samples have been played.
	 *	@param sc The sampled have been played
	 */
	protected void fireAfterPlay( final SampleChunk sc )
	{
		for( final AudioEventListener ael: this.listeners )
			ael.afterPlay( this, sc );
	}
	
	/**
	 * 	Set the timecode object that is updated as the audio is played.
	 *  @param t The timecode object.
	 */
	public void setTimecodeObject( final AudioTimecode t )
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
		this.setMode( Mode.PLAY );
		this.timecodeReadAt = 0;
		if( !this.started )
		{
			this.started = true;
			try
			{
				// Open the sound system.
				this.openJavaSound();
			
				// Read samples until there are no more.
				SampleChunk samples = null;
				boolean ended = false;
				while( !ended && this.mode != Mode.STOP )
				{
					if( this.mode == Mode.PLAY )
					{
						// Get the next sample chunk
						samples = this.stream.nextSampleChunk();
						
						// Check if we've reached the end of the line
						if( samples == null )
						{
							ended = true;
							continue;
						}
						
						// Fire the before event
						this.fireBeforePlay( samples );
						
						// Play the samples
						this.playJavaSound( samples );
						
						// Fire the after event
						this.fireAfterPlay( samples );
	
						// If we have a timecode object to update, we'll update it here
						if( this.currentTimecode != null )
						{
							this.currentTimestamp = samples.getStartTimecode().
									getTimecodeInMilliseconds(); 
							this.timecodeReadAt = System.currentTimeMillis();
							this.currentTimecode.setTimecodeInMilliseconds( this.currentTimestamp );
						}
					}
					else
					{
						// Let's be nice and not loop madly if we're not playing
						// (we must be in PAUSE mode)
						try
						{
							Thread.sleep( 500 );
						}
						catch( final InterruptedException ie )
						{
						}
					}
				}
					
				// Fire the audio ended event
				this.fireAudioEnded( this.stream );
				this.setMode( Mode.STOP );
				this.reset();
			}
			catch( final Exception e )
			{
				e.printStackTrace();
			}
			finally
			{
				// Close the sound system
				this.closeJavaSound();			
			}
		}
		else
		{
			// Already playing something, so we just start going again
			this.setMode( Mode.PLAY );
		}
	}
	
	/**
	 * 	Create a new audio player in a separate thread for playing audio.
	 * 
	 *	@param as The audio stream to play.
	 *	@return The audio player created.
	 */
	public static AudioPlayer createAudioPlayer( final AudioStream as )
	{
		final AudioPlayer ap = new AudioPlayer( as );
		new Thread( ap ).start();
		return ap;
	}

	/**
	 * 	Create a new audio player in a separate thread for playing audio.
	 * 	To find out device names, use {@link AudioUtils#getDevices()}.
	 * 
	 *	@param as The audio stream to play.
	 *	@param device The name of the device to use.
	 *	@return The audio player created.
	 */
	public static AudioPlayer createAudioPlayer( final AudioStream as, final String device )
	{
		final AudioPlayer ap = new AudioPlayer( as, device );
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
			if( this.deviceName != null )
					this.mLine = AudioUtils.getJavaOutputLine( this.deviceName, this.stream.getFormat() );
			else	this.mLine = AudioUtils.getAnyJavaOutputLine( this.stream.getFormat() );

			if( this.mLine == null )
				throw new Exception( "Cannot instantiate a sound line." );
			
			// If no exception has been thrown we open the line.
			this.mLine.open( this.mLine.getFormat(), (int)
					(this.stream.getFormat().getSampleRateKHz() * this.soundLineBufferSize) );

			// If we've opened the line, we start it running
			this.mLine.start();
			
			System.out.println( "Opened Java Sound Line: "+this.mLine.getFormat() );
		}
		catch( final LineUnavailableException e )
		{
			throw new Exception( "Could not open Java Sound audio line for" +
					" the audio format "+this.stream.getFormat() );
		}
	}

	/**
	 * 	Play the given sample chunk to the Java sound line. The line should be
	 * 	set up to accept the samples that we're going to give it, as we did
	 * 	that in the {@link #openJavaSound()} method.
	 * 
	 *	@param chunk The chunk to play.
	 */
	private void playJavaSound( final SampleChunk chunk )
	{
		final byte[] rawBytes = chunk.getSamples();
		this.mLine.write( rawBytes, 0, rawBytes.length );
	}

	/**
	 * 	Close down the Java sound APIs.
	 */
	private void closeJavaSound()
	{
		if( this.mLine != null )
		{
			// Wait for the buffer to empty...
			this.mLine.drain();
			
			// ...then close
			this.mLine.close();
			this.mLine = null;
		}
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.time.TimeKeeper#getTime()
	 */
	@Override
	public AudioTimecode getTime()
	{
		// If we've not yet read any samples, just return the timecode
		// object as it was first given to us.
		if( this.timecodeReadAt == 0 )
			return this.currentTimecode;
		
		// Update the timecode if we're playing (otherwise we'll return the
		// latest timecode)
		if( this.mode == Mode.PLAY )
			this.currentTimecode.setTimecodeInMilliseconds( this.currentTimestamp +
				(System.currentTimeMillis() - this.timecodeReadAt) );
		
		return this.currentTimecode;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.time.TimeKeeper#stop()
	 */
	@Override
	public void stop()
	{
		this.setMode( Mode.STOP );
	}

	/**
	 * 	Set the mode of the player.
	 *	@param m
	 */
	public void setMode( final Mode m )
	{
		this.mode = m;
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.time.TimeKeeper#supportsPause()
	 */
	@Override
	public boolean supportsPause()
	{
		return true;
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.time.TimeKeeper#supportsSeek()
	 */
	@Override
	public boolean supportsSeek()
	{
		return true;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.time.TimeKeeper#seek(long)
	 */
	@Override
	public void seek( final long timestamp )
	{
		this.stream.seek( timestamp );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.time.TimeKeeper#reset()
	 */
	@Override
	public void reset()
	{
		this.timecodeReadAt = 0;
		this.currentTimestamp = 0;
		this.started = false;
		this.currentTimecode.setTimecodeInMilliseconds( 0 );
		this.stream.reset();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.time.TimeKeeper#pause()
	 */
	@Override
	public void pause()
	{
		this.setMode( Mode.PAUSE );
		
		// Set the current timecode to the time at which we paused.
		this.currentTimecode.setTimecodeInMilliseconds( this.currentTimestamp +
				(System.currentTimeMillis() - this.timecodeReadAt) );
	}
}
