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
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 28 Oct 2011
 */
public abstract class AudioGrabber extends AudioStream implements Runnable
{
	/** A list of listeners for samples */
	protected List<AudioGrabberListener> listeners = new ArrayList<AudioGrabberListener>();

	/**
	 * Start the stream grabbing.
	 * 
	 * @inheritDoc
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
}
