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
package org.openimaj.audio.samples;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.SampleChunk;
import org.openimaj.util.array.ArrayUtils;

/**
 * An implementation of a sample buffer that maintains the floating point
 * precision values.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 27 Jul 2012
 * @version $Author$, $Revision$, $Date$
 */
public class FloatSampleBuffer implements SampleBuffer {
	/** The samples */
	private float[] samples = null;

	/** The audio format */
	private AudioFormat format = null;

	/**
	 * @param samples
	 *            The samples to use
	 * @param af
	 *            The audio format of the samples
	 */
	public FloatSampleBuffer(float[] samples, AudioFormat af) {
		this.format = af.clone();
		this.format.setNBits(-1);
		this.samples = samples;
	}

	/**
	 * 
	 * @param samples
	 *            The samples to use
	 * @param af
	 *            The audio format
	 */
	public FloatSampleBuffer(double[] samples, AudioFormat af) {
		this(ArrayUtils.doubleToFloat(samples), af);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.samples.SampleBuffer#get(int)
	 */
	@Override
	public float get(int index) {
		return samples[index];
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.samples.SampleBuffer#set(int, float)
	 */
	@Override
	public void set(int index, float sample) {
		samples[index] = sample;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.samples.SampleBuffer#size()
	 */
	@Override
	public int size() {
		return samples.length;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.samples.SampleBuffer#getFormat()
	 */
	@Override
	public AudioFormat getFormat() {
		return format;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.samples.SampleBuffer#setFormat(org.openimaj.audio.AudioFormat)
	 */
	@Override
	public void setFormat(AudioFormat af) {
		format = af.clone();
		format.setNBits(-1);
	}

	/**
	 * Returns NULL. If you need a sample chunk from this sample buffer, then
	 * you must instantiate the appropriate sample chunk first and fill it. It
	 * cannot be done from this class because this class no longer knows how
	 * many bits you would like the sample chunk to be created as.
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.samples.SampleBuffer#getSampleChunk()
	 */
	@Override
	public SampleChunk getSampleChunk() {
		return null;
	}

	/**
	 * Returns NULL. If you need a sample chunk from this sample buffer, then
	 * you must instantiate the appropriate sample chunk first and fill it. It
	 * cannot be done from this class because this class no longer knows how
	 * many bits you would like the sample chunk to be created as.
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.samples.SampleBuffer#getSampleChunk(int)
	 */
	@Override
	public SampleChunk getSampleChunk(int channel) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.samples.SampleBuffer#asDoubleArray()
	 */
	@Override
	public double[] asDoubleArray() {
		return ArrayUtils.floatToDouble(samples);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.samples.SampleBuffer#getUnscaled(int)
	 */
	@Override
	public float getUnscaled(int index) {
		return get(index);
	}

	/**
	 * Multipy the samples by the given scalar
	 * 
	 * @param scalar
	 *            The scalar
	 * @return this object
	 */
	public FloatSampleBuffer multiply(double scalar) {
		for (int i = 0; i < samples.length; i++)
			set(i, (float) (samples[i] * scalar));
		return this;
	}

	/**
	 * Add the scalar to all the samples
	 * 
	 * @param scalar
	 *            The scalar
	 * @return this object
	 */
	public FloatSampleBuffer add(double scalar) {
		for (int i = 0; i < samples.length; i++)
			set(i, (float) (samples[i] + scalar));
		return this;
	}
}
