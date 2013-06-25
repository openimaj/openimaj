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

import gnu.trove.iterator.TFloatIterator;
import gnu.trove.list.array.TFloatArrayList;

import java.util.Iterator;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.timecode.AudioTimecode;
import org.openimaj.util.array.ArrayUtils;

/**
 * An implementation of a sample buffer that maintains the floating point
 * precision values. Note that this buffer has no timecode associated with it
 * and that {@link SampleChunk}s cannot be retrieved from it.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 27 Jul 2012
 * @version $Author$, $Revision$, $Date$
 */
public class FloatSampleBuffer implements SampleBuffer, Iterator<Float>
{
	/** The samples */
	private float[] samples = null;

	/** The audio format */
	private AudioFormat format = null;

	/** Iterator over the samples in this buffer */
	private TFloatIterator tfIterator;

	/**
	 * @param samples
	 *            The samples to use
	 * @param af
	 *            The audio format of the samples
	 */
	public FloatSampleBuffer(final float[] samples, final AudioFormat af) {
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
	public FloatSampleBuffer(final double[] samples, final AudioFormat af) {
		this(ArrayUtils.convertToFloat(samples), af);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.samples.SampleBuffer#get(int)
	 */
	@Override
	public float get(final int index) {
		return this.samples[index];
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.samples.SampleBuffer#set(int, float)
	 */
	@Override
	public void set(final int index, final float sample) {
		this.samples[index] = sample;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.samples.SampleBuffer#size()
	 */
	@Override
	public int size() {
		return this.samples.length;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.samples.SampleBuffer#getFormat()
	 */
	@Override
	public AudioFormat getFormat() {
		return this.format;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.samples.SampleBuffer#setFormat(org.openimaj.audio.AudioFormat)
	 */
	@Override
	public void setFormat(final AudioFormat af) {
		this.format = af.clone();
		this.format.setNBits(-1);
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
	public SampleChunk getSampleChunk(final int channel) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.samples.SampleBuffer#asDoubleArray()
	 */
	@Override
	public double[] asDoubleArray() {
		return ArrayUtils.convertToDouble(this.samples);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.samples.SampleBuffer#asDoubleChannelArray()
	 */
	@Override
	public double[][] asDoubleChannelArray()
	{
		final int nc = this.format.getNumChannels();
		final double[][] s = new double[nc][this.samples.length / nc];
		for (int c = 0; c < nc; c++)
			for (int sa = 0; sa < this.samples.length / nc; sa++)
				s[c][sa] = this.samples[sa * nc + c];
		return s;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.samples.SampleBuffer#getUnscaled(int)
	 */
	@Override
	public float getUnscaled(final int index) {
		return this.get(index);
	}

	/**
	 * Multipy the samples by the given scalar
	 * 
	 * @param scalar
	 *            The scalar
	 * @return this object
	 */
	public FloatSampleBuffer multiply(final double scalar) {
		for (int i = 0; i < this.samples.length; i++)
			this.set(i, (float) (this.samples[i] * scalar));
		return this;
	}

	/**
	 * Add the scalar to all the samples
	 * 
	 * @param scalar
	 *            The scalar
	 * @return this object
	 */
	public FloatSampleBuffer add(final double scalar) {
		for (int i = 0; i < this.samples.length; i++)
			this.set(i, (float) (this.samples[i] + scalar));
		return this;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Float> iterator()
	{
		this.tfIterator = this.tf_iterator();
		return this;
	}

	/**
	 * Returns a trove float iterator
	 * 
	 * @return a trove float iterator
	 */
	public TFloatIterator tf_iterator()
	{
		final TFloatArrayList l = new TFloatArrayList();
		for (final float f : this.samples)
			l.add(f);
		return l.iterator();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		return this.tfIterator.hasNext();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Float next()
	{
		return this.tfIterator.next();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove()
	{
		this.tfIterator.remove();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.samples.SampleBuffer#getStartTimecode()
	 */
	@Override
	public AudioTimecode getStartTimecode()
	{
		return null;
	}
}
