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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.SampleChunk;

/**
 * 	A {@link SampleBuffer} for 16-bit sample chunks.
 * 
 * 	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	@created 23rd November 2011
 */
public class SampleBuffer16Bit implements SampleBuffer
{
	/** The underlying byte array we're wrapping */
	private byte[] samples = null;
	
	/** The short buffer that we're wrapping */
	private ShortBuffer shortBuffer = null;
	
	/** The audio format of the samples */
	private AudioFormat format;

	/**
	 * 	Create a new 16-bit sample buffer using the given
	 * 	samples and the given audio format.
	 * 
	 * 	@param samples The samples to buffer.
	 * 	@param af The audio format.
	 */
	public SampleBuffer16Bit( SampleChunk samples, AudioFormat af )
	{
		this.format = af;
		this.shortBuffer = samples.getSamplesAsByteBuffer().asShortBuffer();
		this.samples = samples.getSamples();
	}
	
	/**
	 * 	Create a new 16-bit sample buffer using the given
	 * 	sample format at the given size.
	 * 
	 * 	@param af The audio format of the samples
	 * 	@param nSamples The number of samples
	 */
	public SampleBuffer16Bit( AudioFormat af, int nSamples )
	{
		this.format = af;
		this.samples = new byte[ nSamples * af.getNumChannels() * 2 ];
		this.shortBuffer = new SampleChunk(this.samples,this.format)
			.getSamplesAsByteBuffer().asShortBuffer();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#getSampleChunk()
	 */
	@Override
	public SampleChunk getSampleChunk()
	{
		return new SampleChunk( this.samples, this.format );
	}
	
	/**
	 *	{@inheritDoc}
	 *
	 *	Note that because we cannot use native methods for copying parts of
	 *	an array, we must use Java methods so this will be considerably
	 *	slower than {@link #getSampleChunk()}.
	 *
	 * 	@see org.openimaj.audio.samples.SampleBuffer#getSampleChunk(int)
	 */
	@Override
	public SampleChunk getSampleChunk( int channel )
	{
		if( channel > format.getNumChannels() )
			throw new IllegalArgumentException( "Cannot generate sample chunk " +
					"for channel "+channel+" as sample only has " + 
					format.getNumChannels() + " channels." );
		
		if( channel == 0 && format.getNumChannels() == 1 )
			return getSampleChunk();
		
		byte[] newSamples = new byte[size()*2];
		ShortBuffer sb = ByteBuffer.wrap( newSamples ).order(
			format.isBigEndian()?ByteOrder.BIG_ENDIAN:ByteOrder.LITTLE_ENDIAN ).
			asShortBuffer();
		for( int i = 0; i < size()/format.getNumChannels(); i++ )
			sb.put( i, shortBuffer.get( i*format.getNumChannels() + channel ) );
		
		AudioFormat af = format.clone();
		af.setNumChannels( 1 );
		return new SampleChunk( newSamples, af );
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#get(int)
	 */
	@Override
	public float get( int index ) 
	{
		if( index >= shortBuffer.limit() )
			return 0;
		
		// Convert the short to an integer
		return shortBuffer.get(index) << 16;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#getUnscaled(int)
	 */
	public float getUnscaled( int index )
	{
		return shortBuffer.get(index);
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#set(int, float)
	 */
	@Override
	public void set( int index, float sample ) 
	{
		shortBuffer.put( index, (short)(((int)sample)>>16) );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#size()
	 */
	@Override
	public int size() 
	{
		return shortBuffer.limit();
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#getFormat()
	 */
	@Override
	public AudioFormat getFormat()
	{
		return format;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#setFormat(org.openimaj.audio.AudioFormat)
	 */
	@Override
	public void setFormat( AudioFormat af )
	{
		this.format = af;
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#asDoubleArray()
	 */
	@Override
	public double[] asDoubleArray()
	{
		double[] d = new double[size()];
		for( int i = 0; i < size(); i++ )
			d[i] = get(i);
		return d;
	}
}
