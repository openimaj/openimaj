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

/**
 *	This class encapsulates the information that determines the format
 *	of audio data.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 8 Jun 2011
 *	
 */
public class AudioFormat
{
	/** The number of channels for each sample */
	private int nChannels = 0;
	
	/** The number of bits in each sample */
	private int nBits = 0;
	
	/** The sample rate in KHz */
	private double sampleRateKHz = 0;
	
	/** Whether the data is signed */
	private boolean isSigned = true;
	
	/** Whether the data is big-endian */
	private boolean isBigEndian = false;
	
	/**
	 * 	Construct a new audio format object with the
	 * 	given number of bits and sample rate.
	 * 
	 *	@param nBits The number of bits in each sample
	 *	@param sampleRate The sample rate in kilohertz
	 *	@param nChannels The number of channels
	 */
	public AudioFormat( int nBits, double sampleRate, int nChannels )
	{
		this.nBits = nBits;
		this.sampleRateKHz = sampleRate;
		this.setNumChannels( nChannels );
	}

	/**
	 * 	Get the number of bits in each sample. 
	 *	@return The number of bits in each sample.
	 */
	public int getNBits()
	{
		return nBits;
	}
	
	/**
	 * 	Sets the number of bits in this audio format. This is expected to
	 * 	be a multiple of 8, but can be -1 for a non-integer format.
	 * 
	 *	@param nBits The number of bits.
	 *	@return this audio format
	 */
	public AudioFormat setNBits( int nBits )
	{
		this.nBits = nBits;
		return this;
	}

	/**
	 * 	Get the rate at which the audio should be replayed
	 *	@return The audio sample rate in kilohertz.
	 */
	public double getSampleRateKHz()
	{
		return sampleRateKHz;
	}
	
	/**
	 * 	Set the sample rate at which the audio should be replayed
	 *	@param s The sample rate
	 *	@return this audio format
	 */
	public AudioFormat setSampleRateKHz( double s )
	{
		this.sampleRateKHz = s;
		return this;
	}

	/**
	 * 	Set the number of channels in this format.
	 *	@param nChannels the number of channels
	 *	@return this audio format
	 */
	public AudioFormat setNumChannels( int nChannels )
	{
		this.nChannels = nChannels;
		return this;
	}

	/**
	 * 	Get the number of channels in this format.
	 *	@return the number of channels
	 */
	public int getNumChannels()
	{
		return nChannels;
	}

	/**
	 * 	Set whether the data is signed or not.
	 *	@param isSigned Whether the data is signed.
	 *	@return this audio format
	 */
	public AudioFormat setSigned( boolean isSigned )
	{
		this.isSigned = isSigned;
		return this;
	}

	/**
	 * 	Returns whether the data is signed or unsigned.
	 *	@return TRUE if the data is signed; FALSE otherwise;
	 */
	public boolean isSigned()
	{
		return isSigned;
	}

	/**
	 * 	Set whether the data is big-endian or not.
	 *	@param isBigEndian Whether the data is big-endian
	 *	@return this audio format
	 */
	public AudioFormat setBigEndian( boolean isBigEndian )
	{
		this.isBigEndian = isBigEndian;
		return this;
	}

	/**
	 * 	Returns whether the data is big or little endian.
	 *	@return TRUE if the data is big-endian; FALSE if little-endian
	 */
	public boolean isBigEndian()
	{
		return isBigEndian;
	}
	
	/**
	 *	{@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return "[Audio: "+getSampleRateKHz()+"KHz, "+getNBits()+"bit, "+
			getNumChannels()+" channel"+(getNumChannels()>1?"s":"")+
			", "+(isSigned?"signed":"unsigned")+", "+
			(isBigEndian?"big-endian":"little-endian")+"]";
	}

	/**
	 *  {@inheritDoc}
	 *  @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj )
	{
		if( obj instanceof AudioFormat )
		{
			AudioFormat af = (AudioFormat)obj;
			if( isBigEndian == af.isBigEndian &&
				isSigned == af.isSigned &&
				nChannels == af.nChannels &&
				nBits == af.nBits &&
				sampleRateKHz == af.sampleRateKHz )
				return true;
		}
		
		return false;
	}
	
	/**
	 *	{@inheritDoc}
	 */
	@Override
	public AudioFormat clone()
	{
		AudioFormat af = new AudioFormat( 
				getNBits(), getSampleRateKHz(), getNumChannels() );
		af.setBigEndian( isBigEndian );
		af.setSigned( isSigned );
		return af;
	}
	
	/**
	 * 	Get a Java Sound API AudioFormat object using this object's
	 * 	properties.
	 * 
	 *	@return The Java Sound API Audio Format object.
	 */
	public javax.sound.sampled.AudioFormat getJavaAudioFormat()
	{
		// Convert the OpenIMAJ audio format to a Java Sound audio format object
		return new javax.sound.sampled.AudioFormat(
		        (int)this.getSampleRateKHz() * 1000, 
		        this.getNBits(), this.getNumChannels(), 
		        this.isSigned(), this.isBigEndian() );
	}
}
