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

/**
 * 	An abstract class for objects that are able to act as audio grabbers.
 * 	Audio grabbers should be able to provide streams of audio data, hence
 * 	they extend the {@link AudioStream} abstract class. Obviously, if using
 * 	an audio grabber in the context of an audio stream (for processing or
 * 	whatever), it cannot be guaranteed that the audio grabber will buffer
 * 	sampled between calls of {@link #nextSampleChunk()}.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 28 Oct 2011
 */
public abstract class AudioGrabber extends AudioStream implements Runnable
{
	/** A list of listeners for samples */
	protected List<AudioGrabberListener> listeners = new ArrayList<AudioGrabberListener>();

	/**
	 * Start the stream grabbing.
	 * 
	 * {@inheritDoc}
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public abstract void run();
	
	/**
	 * Stop the stream grabbing
	 */
	public abstract void stop();
	
	/**
	 * Whether the stream is stopped or not.
	 * @return TRUE if the stream is stopped; FALSE otherwise
	 */
	public abstract boolean isStopped();
	
	/**
	 * 	Fire the event and audio is now available
	 */
	protected abstract void fireAudioAvailable();
	
	/**
	 * Add an audio grabber listener to this audio grabber.
	 * @param l The listener to add.
	 */
	public void addAudioGrabberListener( AudioGrabberListener l )
	{
		listeners.add( l );
	}

	/**
	 * Remove an audio grabber listener from this audio grabber.
	 * @param l The audio grabber listener to remove.
	 */
	public void removeAudioGrabberListener( AudioGrabberListener l )
	{
		listeners.remove( l );
	}
	
	/**
	 *	Does nothing by default for an audio grabber.
	 * 	@see org.openimaj.audio.AudioStream#reset()
	 */
	@Override
	public void reset()
	{
	}
}
