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
package org.openimaj.audio.processor;

import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;

/**
 * An interface for objects that are able to process audio sample data. Due to
 * the fact that audio processors provide processed audio, they are also able to
 * implement the {@link AudioStream} interface thereby making processors
 * chainable.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 8 Jun 2011
 *
 */
public abstract class AudioProcessor extends AudioStream
{
	/** The audio stream to process in a chain */
	private AudioStream stream = null;

	/**
	 * A default constructor for processing sample chunks or files in an ad-hoc
	 * manner.
	 */
	public AudioProcessor()
	{
	}

	/**
	 * Construct a new processor based on the given stream. This processor can
	 * then be used as a stream itself in a chain.
	 *
	 * @param a
	 *            The audio stream to process.
	 */
	public AudioProcessor(final AudioStream a)
	{
		this.stream = a;
		if (a != null)
			this.format = a.getFormat().clone();
	}

	/**
	 * Function to process a whole audio stream. If the process returns null, it
	 * will stop the processing of the audio stream. Note that the output of the
	 * audio stream processing is not stored (it may be a live stream and so
	 * would be too large to store), so the caller must interact with the audio
	 * processor themselves to retrieve any useful information from the
	 * processing.
	 *
	 * @param a
	 *            The audio stream to process.
	 * @throws Exception
	 *             If the processing failed
	 */
	public void process(final AudioStream a) throws Exception
	{
		this.stream = a;
		while (this.nextSampleChunk() != null)
			;
		this.processingComplete(a);
	}

	/**
	 * Function that takes a sample chunk and processes the chunk. It should
	 * also return a sample chunk containing the processed data. If wished, the
	 * chunk may be side-affected and the input chunk returned. It should not be
	 * assumed that the input chunk will be side-affected, but it must be noted
	 * that it is possible that it could be. This process function may also
	 * return null. If null is returned it means that the rest of the audio
	 * stream is not required to be processed by this processing function.
	 * Whether the rest of the sample chunks are copied or ignored is up to the
	 * caller.
	 *
	 * @param sample
	 *            The sample chunk to process.
	 * @return A sample chunk containing processed data.
	 * @throws Exception
	 *             If the processing could not take place
	 */
	public abstract SampleChunk process(SampleChunk sample) throws Exception;

	/**
	 * Called when the processing of a given audio stream has been completed.
	 * This can be used to alter the audio stream's properties.
	 *
	 * @param a
	 *            The audio stream that has finished processing.
	 */
	public void processingComplete(final AudioStream a)
	{
		// Default is no implementation. Override this if necessary.
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.audio.AudioStream#nextSampleChunk()
	 */
	@Override
	public SampleChunk nextSampleChunk()
	{
		try
		{
			final SampleChunk s = this.stream.nextSampleChunk();
			return (s != null ? this.process(s) : null);
		} catch (final Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get the underlying stream. Will return null for non-chained audio
	 * processors.
	 *
	 * @return The underlying stream on chained processors.
	 */
	public AudioStream getUnderlyingStream()
	{
		return this.stream;
	}

	/**
	 * Sets the underlying stream, allowing it to be changed.
	 *
	 * @param stream
	 *            The stream
	 */
	public void setUnderlyingStream(final AudioStream stream)
	{
		this.stream = stream;
		if (stream != null)
			this.format = stream.getFormat().clone();
	}

	/**
	 * {@inheritDoc}
	 *
	 * The implementation in this class just calls reset on the wrapped stream
	 * (if it's set).
	 *
	 * @see org.openimaj.audio.AudioStream#reset()
	 */
	@Override
	public void reset()
	{
		if (this.stream != null) {
			stream.reset();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.audio.AudioStream#getLength()
	 */
	@Override
	public long getLength()
	{
		return this.stream.getLength();
	}
}
