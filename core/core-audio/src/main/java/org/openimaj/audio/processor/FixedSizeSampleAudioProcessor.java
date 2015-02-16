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
 * Provides an audio processor that will process sample chunks of specific sizes
 * when the incoming stream's sample chunk size is unknown.
 * <p>
 * This class has applications for FFT (for example) where the input sample size
 * must be a power of 2 and the underlying audio stream reader may be returning
 * sample chunks of any size.
 * <p>
 * The processor can also provide overlapping sample windows. Call
 * {@link #setWindowStep(int)} to determine the slide of each sliding window. If
 * this is set to 0 or below, the windows will be consecutive and will not
 * overlap.
 * <p>
 * The only assumption made by the class about the samples is that they are
 * whole numbers of bytes (8, 16, 24, 32 bits etc.). This is a pretty reasonable
 * assumption.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *
 * @created 11 Jul 2011
 */
public class FixedSizeSampleAudioProcessor extends AudioProcessor
{
	/** The size of each required sample chunk */
	private int requiredSampleSetSize = 512;

	/** Our buffer of sample chunks stored between calls to process() */
	private SampleChunk sampleBuffer = null;

	/** The number of samples overlap required between each window */
	private int windowStep = 0;

	/** Whether or not the windows are overlapping */
	private boolean overlapping = false;

	/**
	 * Create processor that will process chunks of the given size.
	 *
	 * @param sizeRequired
	 *            The size of the chunks required (in samples)
	 */
	public FixedSizeSampleAudioProcessor(final int sizeRequired)
	{
		this.requiredSampleSetSize = sizeRequired;
	}

	/**
	 * Create processor that will process chunks of the given size.
	 *
	 * @param stream
	 *            An audio stream to process
	 * @param sizeRequired
	 *            The size of the chunks required (in samples)
	 */
	public FixedSizeSampleAudioProcessor(final AudioStream stream, final int sizeRequired)
	{
		super(stream);
		this.requiredSampleSetSize = sizeRequired;
	}

	/**
	 * Constructor that takes the size of the window and the size of the window
	 * overlap.
	 *
	 * @param nSamplesInWindow
	 *            The number of samples in the window
	 * @param nSamplesOverlap
	 *            The size of the overlap
	 */
	public FixedSizeSampleAudioProcessor(final int nSamplesInWindow,
			final int nSamplesOverlap)
	{
		this(nSamplesInWindow);
		this.setWindowStep(nSamplesOverlap);
	}

	/**
	 * Chainable constructor that takes the size of the window and the number of
	 * samples overlap.
	 *
	 * @param as
	 *            The chained audio stream
	 * @param nSamplesInWindow
	 *            Samples in window
	 * @param nSamplesOverlap
	 *            Samples in window overlap
	 */
	public FixedSizeSampleAudioProcessor(final AudioStream as, final int nSamplesInWindow,
			final int nSamplesOverlap)
	{
		this(as, nSamplesInWindow);
		this.setWindowStep(nSamplesOverlap);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.audio.processor.AudioProcessor#nextSampleChunk()
	 */
	@Override
	public SampleChunk nextSampleChunk()
	{
		// System.out.println( "Sample Buffer: "+(sampleBuffer != null?
		// sampleBuffer.getNumberOfSamples() : "null"));

		// Get the samples. If there's more samples than we need in the
		// buffer, we'll just use that, otherwise we'll get a new sample
		// chunk from the stream.
		SampleChunk s = null;
		if (this.sampleBuffer != null &&
				this.sampleBuffer.getNumberOfSamples() >= this.requiredSampleSetSize)
		{
			s = this.sampleBuffer;
			this.sampleBuffer = null;
		}
		else
		{
			s = this.getUnderlyingStream().nextSampleChunk();
			if (s != null)
				s = s.clone();

			// If we have something in our buffer, prepend it to the new
			// sample chunk
			if (this.sampleBuffer != null && this.sampleBuffer.getNumberOfSamples() > 0
					&& s != null)
			{
				// Prepend the contents of the sample buffer to the new sample
				// chunk
				s.prepend(this.sampleBuffer);
				this.sampleBuffer = null;
			}
		}

		// Sample buffer will always be null here
		// It will be reinstated later with the left-overs after processing.
		// From this point on we'll only work on the SampleChunk s.

		// Catch the end of the stream. As the sample buffer is always empty
		// at this point, the only time s can be null is that if the
		// nextSampleChunk() above returned null. In which case, there's no
		// more audio, so we return null.
		if (s == null)
		{
			if (this.sampleBuffer != null)
			{
				s = this.sampleBuffer;
				this.sampleBuffer = null;
				return s;
			}
			else
				return null;
		}

		// Now check how many samples we have to start with
		int nSamples = s.getNumberOfSamples();

		// If we don't have enough samples, we'll keep getting chunks until
		// we have enough or until the end of the stream is reached.
		boolean endOfStream = false;
		while (!endOfStream && nSamples < this.requiredSampleSetSize)
		{
			final SampleChunk nextSamples = this.getUnderlyingStream().nextSampleChunk();
			if (nextSamples != null)
			{
				// Append the new samples onto the end of the sample chunk
				s.append(nextSamples);

				// Check how many samples we now have.
				nSamples = s.getNumberOfSamples();
			}
			else
				endOfStream = true;
		}

		// If we have the right number of samples,
		// or we've got to the end of the stream
		// then we just return the chunk we have.
		SampleChunk ss;
		if (!endOfStream && (this.overlapping || nSamples > this.requiredSampleSetSize))
		{
			// We must now have too many samples...
			// Store the excess back into the buffer
			int start = 0;
			if (this.overlapping)
				start = this.windowStep;
			else
				start = this.requiredSampleSetSize;

			// Store the rest into the sample buffer
			this.sampleBuffer = s.getSampleSlice(start, nSamples - start);

			// Process a slice of the sample chunk
			ss = s.getSampleSlice(0, this.requiredSampleSetSize);
		}
		else
		{
			ss = s;

			if (ss.getNumberOfSamples() < this.requiredSampleSetSize)
				ss.pad(this.requiredSampleSetSize);
		}

		try
		{
			// Return the processed samples
			return this.process(ss);
		} catch (final Exception e)
		{
			// If there's an error, log it and return the unprocessed samples
			e.printStackTrace();
			return ss;
		}
	}

	/**
	 * Set the step of each overlapping window.
	 *
	 * @param overlap
	 *            The step of each overlapping window.
	 */
	public void setWindowStep(final int overlap)
	{
		this.windowStep = overlap;
		this.overlapping = true;
		if (overlap <= 0)
			this.overlapping = false;
	}

	/**
	 * Returns the step of each overlapping window.
	 *
	 * @return The step of each overlapping window.
	 */
	public int getWindowStep()
	{
		return this.windowStep;
	}

	/**
	 * Set the size of the window required. Should be called before the object
	 * has been used to process anything. The result is undefined if it's called
	 * during processing and will probably lead to some sort of bounds error.
	 *
	 * @param sizeRequired
	 *            The size required.
	 */
	public void setWindowSize(final int sizeRequired)
	{
		this.requiredSampleSetSize = sizeRequired;
	}

	/**
	 * Returns the size of the sample window.
	 *
	 * @return The size of the sample window.
	 */
	public int getWindowSize()
	{
		return this.requiredSampleSetSize;
	}

	/**
	 * Returns whether the windows are overlapping or not.
	 *
	 * @return whether the windows are overlapping or not.
	 */
	public boolean isOverlapping()
	{
		return this.overlapping;
	}

	/**
	 * {@inheritDoc}
	 *
	 * The default operation of the {@link FixedSizeSampleAudioProcessor} is
	 * simply to change the shape of the sample chunk. You may override this
	 * method to process the samples directly.
	 *
	 * @see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process(final SampleChunk sample) throws Exception
	{
		return sample;
	}
}
