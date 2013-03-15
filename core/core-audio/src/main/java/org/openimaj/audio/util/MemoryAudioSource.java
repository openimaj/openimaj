/**
 *
 */
package org.openimaj.audio.util;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;

/**
 *	Provides a buffered audio source such that the audio can be reset
 *	and re-read as many times as needed without the need for redecoding.
 *	You should pass your audio stream through this method if you need
 *	to do certain complete-source calculations on the data (such as
 *	finding peak or means) prior to some other processing. Clearly,
 *	the limitation to how long an audio source you can store with this
 *	class is the memory of your machine, and remember that the audio
 *	samples are stored uncompressed, so they take up a lot of room! If
 *	you're planning on performing complete-source calculations on large
 *	audio data, it is recommended to stream the data multiple times.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 4 Mar 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class MemoryAudioSource extends AudioStream
{
	/** The original audio stream */
	private AudioStream stream = null;

	/** The buffered sample list */
	private final List<SampleChunk> buffer = new ArrayList<SampleChunk>();

	/** The current index in the buffer being read */
	private int currentIndex = -1;

	/** The number of milliseconds of data in the buffer */
	private long bufferLength = 0;

	/**
	 * 	Constructor that takes the audio stream to buffer.
	 *	@param as The audio stream
	 */
	public MemoryAudioSource( final AudioStream as )
	{
		this.stream = as;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.AudioStream#nextSampleChunk()
	 */
	@Override
	public SampleChunk nextSampleChunk()
	{
		// See if we're reading from the buffer and if so, return
		// the current sample chunk in the buffer (and move to the next index)
		if( !this.buffer.isEmpty() && this.currentIndex < this.buffer.size() )
			return this.buffer.get( this.currentIndex++ -1 );
		else
		{
			// If we're outside of the buffer, we'll try to get the next
			// sample chunk from the original stream...
			final SampleChunk sc = this.stream.nextSampleChunk();

			// If we get one, we'll add it to the buffer then return it...
			if( sc != null )
			{
				this.buffer.add( sc );
				this.currentIndex++;
				this.bufferLength += (long)(sc.getNumberOfSamples()
						/ sc.getFormat().getSampleRateKHz() );
				return sc;
			}
			// Otherwise we're at the end of the buffer and the original
			// stream, so we'll just return null (end of stream).
			else
				return null;
		}
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.AudioStream#reset()
	 */
	@Override
	public void reset()
	{
		this.currentIndex = 0;
	}

	/**
	 * 	For live streams, returns the length of the buffered audio,
	 * 	otherwise returns the length of the original stream.
	 *
	 * 	@see org.openimaj.audio.AudioStream#getLength()
	 */
	@Override
	public long getLength()
	{
		if( this.stream.getLength() == -1 )
			return this.bufferLength;
		else	return this.stream.getLength();
	}
}
