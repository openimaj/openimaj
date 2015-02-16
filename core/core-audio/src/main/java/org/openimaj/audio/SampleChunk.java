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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.audio.samples.SampleBufferFactory;
import org.openimaj.audio.timecode.AudioTimecode;

/**
 * Represents a chunk of an audio file and stores the raw audio data. The data
 * is unnormalised - that is, it is stored in this class in its original format
 * in the form of a byte array. This is for speed during audio playback.
 *
 * If you require normalised data (data as an integer array for example) use the
 * method {@link #getSamplesAsByteBuffer()} and use the {@link ByteBuffer}'s
 * methods asXXXBuffer (e.g. ByteBuffer#asShortBuffer) to get the samples in a
 * normalised form.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 8 Jun 2011
 *
 */
public class SampleChunk extends Audio
{
	/** The samples in the chunk */
	private byte[] samples = new byte[1];

	/** The timecode of the start of the sample chunk */
	private AudioTimecode startTimecode = new AudioTimecode(0);

	/**
	 * Create a new SampleChunk buffer with the given audio format, but do not
	 * initialise the samples.
	 *
	 * @param af
	 *            The audio format of the samples
	 */
	public SampleChunk(final AudioFormat af)
	{
		this(new byte[1], af);
	}

	/**
	 * Create a new sample chunk using the given samples and the given audio
	 * format.
	 *
	 * @param samples
	 *            The samples to initialise with
	 * @param af
	 *            The audio format of the samples
	 */
	public SampleChunk(final byte[] samples, final AudioFormat af)
	{
		this.setSamples(samples);
		super.format = af;
	}

	/**
	 * Create a new sample chunk using the given samples and the given audio
	 * format.
	 *
	 * @param samples
	 *            The samples to initialise with
	 * @param af
	 *            The audio format of the samples
	 * @param tc
	 *            The audio timecode of these samples
	 */
	public SampleChunk(final byte[] samples, final AudioFormat af, final AudioTimecode tc)
	{
		this.setSamples(samples);
		this.startTimecode = tc;
		super.format = af;
	}

	/**
	 * Set the samples in this sample chunk.
	 *
	 * @param samples
	 *            the samples in this sample chunk.
	 */
	public void setSamples(final byte[] samples)
	{
		synchronized (this.samples)
		{
			this.samples = samples;
		}
	}

	/**
	 * Get the samples in this sample chunk
	 *
	 * @return the samples in this sample chunk
	 */
	public byte[] getSamples()
	{
		return this.samples;
	}

	/**
	 * Returns the number of samples in this sample chunk. If there are 128
	 * stereo samples, this method will return 256. That is, it does not
	 * normalise for the number of channels. However, it does normalise for the
	 * size of each sample. So if this is a 16-bit buffer of 256 bytes length,
	 * this method will return 128.
	 *
	 * @return the number of samples in this sample chunk.
	 */
	public int getNumberOfSamples()
	{
		return this.samples.length / (this.format.getNBits() / 8);
	}

	/**
	 * Returns a {@link ByteBuffer} that can be used to create views of the
	 * samples in the object. For example, to get short integers, you can get
	 * {@link #getSamplesAsByteBuffer()}.asShortBuffer()
	 *
	 * @return A {@link ByteBuffer}
	 */
	public ByteBuffer getSamplesAsByteBuffer()
	{
		if (this.samples == null)
			return null;

		ByteOrder bo = null;

		if (this.format.isBigEndian())
			bo = ByteOrder.BIG_ENDIAN;
		else
			bo = ByteOrder.LITTLE_ENDIAN;

		return ByteBuffer.wrap(this.samples).order(bo);
	}

	/**
	 * Returns an appropriate sample buffer for this sample chunk. If an
	 * appropriate sample buffer cannot be found, null will be returned. This
	 * will wrap the samples array from the underlying chunk, so you should only
	 * side-affect the samples if you're sure they will not be reused.
	 *
	 * @return An appropriate {@link SampleBuffer}
	 */
	public SampleBuffer getSampleBuffer()
	{
		return SampleBufferFactory.createSampleBuffer(this, this.format);
	}

	/**
	 * Set the timecode at the start of this audio chunk.
	 *
	 * @param startTimecode
	 *            the timecode at the start of the chunk.
	 */
	public void setStartTimecode(final AudioTimecode startTimecode)
	{
		this.startTimecode = startTimecode;
	}

	/**
	 * Get the timecode at the start of this audio chunk.
	 *
	 * @return the timecode at the start of the chunk.
	 */
	public AudioTimecode getStartTimecode()
	{
		return this.startTimecode;
	}

	/**
	 * Return a slice of data from the sample array. The indices are based on
	 * the samples in the byte array, not the bytes themselves.
	 * <p>
	 * The assumption is that samples are whole numbers of bytes. So, if the
	 * sample size was 16-bits, then passing in 2 for the start index would
	 * actually index the byte at index 4 in the underlying sample array. The
	 * order of the bytes is unchanged.
	 *
	 * @param start
	 *            The start index of the sample.
	 * @param length
	 *            The length of the samples get.
	 * @return The sample slice as a new {@link SampleChunk}
	 */
	public SampleChunk getSampleSlice(final int start, final int length)
	{
		final int nBytesPerSample = this.format.getNBits() / 8;
		final int startSampleByteIndex = start * nBytesPerSample;
		final byte[] newSamples = new byte[length * nBytesPerSample];

		synchronized (this.samples)
		{
			System.arraycopy(this.samples, startSampleByteIndex,
					newSamples, 0, length * nBytesPerSample);
		}

		final SampleChunk s = new SampleChunk(this.format);
		s.setSamples(newSamples);

		// Set the timestamp to the start of this new slice
		final double samplesPerChannelPerMillisec = this.format.getSampleRateKHz();
		s.setStartTimecode(new AudioTimecode(
				this.getStartTimecode().getTimecodeInMilliseconds() +
				(long) (start / samplesPerChannelPerMillisec)));

		return s;
	}

	/**
	 * Prepends the given samples to the front of this sample chunk. It is
	 * expected that the given samples are in the same format as this sample
	 * chunk; if they are not an exception is thrown. Side-affects this sample
	 * chunk and will return a reference to this sample chunk.
	 *
	 * @param sample
	 *            the samples to add
	 * @return This sample chunk with the bytes prepended
	 */
	public SampleChunk prepend(final SampleChunk sample)
	{
		// Check the sample formats are the same
		if (!sample.getFormat().equals(this.format))
			throw new IllegalArgumentException("Sample types are not equivalent");

		// Get the samples from the given chunk
		final byte[] x1 = sample.getSamplesAsByteBuffer().array();

		// Create an array for the concatenated pair
		final byte[] newSamples = new byte[this.samples.length + x1.length];

		// Loop through adding the new samples
		System.arraycopy(x1, 0, newSamples, 0, x1.length);

		synchronized (this.samples)
		{
			System.arraycopy(this.samples, 0, newSamples, x1.length, this.samples.length);
		}

		// Update this object
		this.samples = newSamples;
		this.setStartTimecode(sample.getStartTimecode().clone());
		return this;
	}

	/**
	 * Appends the given samples to the end of this sample chunk. It is expected
	 * that the given samples are in the same format as this sample chunk; if
	 * they are not an exception is thrown. Side-affects this sample chunk and
	 * will return a reference to this sample chunk.
	 *
	 * @param sample
	 *            the samples to add
	 * @return This sample chunk with the bytes appended
	 */
	public SampleChunk append(final SampleChunk sample)
	{
		// Check the sample formats are the same
		if (!sample.getFormat().equals(this.format))
			throw new IllegalArgumentException("Sample types are not equivalent");

		// Get the samples from the given chunk
		final byte[] x1 = sample.getSamplesAsByteBuffer().array();

		// Create an array for the concatenated pair
		final byte[] newSamples = new byte[this.samples.length + x1.length];

		synchronized (this.samples)
		{
			System.arraycopy(this.samples, 0, newSamples, 0, this.samples.length);
		}

		System.arraycopy(x1, 0, newSamples, this.samples.length, x1.length);

		// Update this object
		this.samples = newSamples;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SampleChunk clone()
	{
		return new SampleChunk(this.samples.clone(), this.format.clone(), this.startTimecode == null ? new AudioTimecode(
				0)
		: this.startTimecode.clone());
	}

	/**
	 * Pads the sample chunk to the given size with zeros.
	 *
	 * @param requiredSampleSetSize
	 *            The required sample size
	 */
	public void pad(final int requiredSampleSetSize)
	{
		final byte[] samples = new byte[requiredSampleSetSize * (this.format.getNBits() / 8)];
		System.arraycopy(this.samples, 0, samples, 0, this.samples.length);
		this.samples = samples;
	}
}
