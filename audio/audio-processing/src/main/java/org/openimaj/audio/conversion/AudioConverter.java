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
package org.openimaj.audio.conversion;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.AudioProcessor;

/**
 *	A class that will work out which processors to instantiate to provide
 *	with a complete conversion from one audio format to another.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 13 May 2013
 */
public class AudioConverter extends AudioProcessor
{
	/** The input format */
	private AudioFormat inputFormat = null;

	/** The calculated audio processor */
	private AudioProcessor processor = null;

	/**
	 * 	Chainable constructor that takes the stream to chain and the
	 * 	output format to convert the stream to.
	 *	@param stream The stream to chain to
	 *	@param output The required output format
	 */
	public AudioConverter( final AudioStream stream, final AudioFormat output )
	{
		super( stream );
		this.inputFormat = stream.getFormat().clone();
		this.setFormat( output.clone() );
		this.processor = AudioConverter.calculateProcess( this.inputFormat, output );
	}

	/**
	 * 	Constructor that takes the input and output formats.
	 *	@param input The input format
	 *	@param output The output format
	 */
	public AudioConverter( final AudioFormat input, final AudioFormat output )
	{
		this.inputFormat = input.clone();
		this.setFormat( output.clone() );
		this.processor = AudioConverter.calculateProcess( input, output );
	}

	/**
	 * 	Calculates the chain of processors that will convert from one format to the
	 * 	other and will return the first in the chain. If there is no processing to be
	 * 	done the method will return null.
	 *
	 *	@param input The input format
	 *	@param output The output format.
	 *	@return The first processor in the stream.
	 */
	public static AudioProcessor calculateProcess( final AudioFormat input, final AudioFormat output )
	{
		// If the input and output formats are the same, then there's
		// no processing to do, so we return null.
		if( input.equals( output ) )
			return null;

		AudioProcessor ap = null;

		// Note that we construct this chain back-to-front. So, if
		// all the processors will be used, then the channel processor,
		// then sample rate, then bit depth processor.  This should be
		// the most efficient order.

		// Check if the endian is the same.
		if( input.isBigEndian() != output.isBigEndian() )
			throw new IllegalArgumentException( "Cannot convert "+input+" to "+output+
					". There is no endian conversion implemented yet." );

		// Check if the signedness is the same
		if( input.isSigned() != output.isSigned() )
			throw new IllegalArgumentException( "Cannot convert "+input+" to "+output+
					". There is no sign conversion implemented yet." );

		// Check if the sample rates are different
		if( input.getSampleRateKHz() != output.getSampleRateKHz() )
			ap = AudioConverter.getProcessor( ap, new SampleRateConverter(
					SampleRateConverter.SampleRateConversionAlgorithm.LINEAR_INTERPOLATION,
						AudioConverter.getFormatSR( ap, output ) ) );

		// Check if the bits are different
		if( input.getNBits() != output.getNBits() )
			ap = AudioConverter.getProcessor( ap, new BitDepthConverter(
					BitDepthConverter.BitDepthConversionAlgorithm.NEAREST,
						AudioConverter.getFormatBits( ap, output ) ) );

		// Check if the channels are different
		if( input.getNumChannels() != output.getNumChannels() )
		{
			if( output.getNumChannels() == 1 )
					ap = AudioConverter.getProcessor( ap, new MultichannelToMonoProcessor() );
			else	throw new IllegalArgumentException( "Cannot convert "+input+" to "+output+
								". Unable to find an appropriate channel converter." );
		}

		return ap;
	}

	/**
	 * 	Updates the number of bits in the source processor's format to match the output format.
	 *
	 *	@param ap The source processor
	 *	@param output The output format
	 *	@return The fixed source format
	 */
	private static AudioFormat getFormatBits( final AudioProcessor ap , final AudioFormat output )
	{
		if( ap == null ) return output;
		final AudioFormat f = ap.getFormat().clone();
		f.setNBits( output.getNBits() );
		return f;
	}

	/**
	 * 	Updates the sample rate in the source processor's format to match the output format.
	 *
	 *	@param ap The source processor
	 *	@param output The output format
	 *	@return The fixed source format
	 */
	private static AudioFormat getFormatSR( final AudioProcessor ap, final AudioFormat output )
	{
		if( ap == null ) return output;
		final AudioFormat f = ap.getFormat().clone();
		f.setSampleRateKHz( output.getSampleRateKHz() );
		return f;
	}

	/**
	 *	Gets the top in the chain of processors (when constructing the chain in reverse order).
	 *	That is, the second processor will always be returned from this method. If the first
	 *	processor is not null, it will be made the source stream for the second processor.
	 *
	 *	@param ap The source processor
	 *	@param ap2 The destination processor
	 *	@return The destination processor
	 */
	private static AudioProcessor getProcessor( final AudioProcessor ap, final AudioProcessor ap2 )
	{
		if( ap == null )
			return ap2;
		ap2.setUnderlyingStream( ap );
		return ap2;
	}

	@Override
	public SampleChunk process( final SampleChunk sample ) throws Exception
	{
		return this.processor.process( sample );
	}

	/**
	 * 	Same as {@link #getFormat()} for this class - returns the output format.
	 *	@return The output format.
	 */
	public AudioFormat getOutputFormat()
	{
		return this.getFormat();
	}
}
