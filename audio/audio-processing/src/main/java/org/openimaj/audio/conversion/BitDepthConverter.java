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
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.audio.samples.SampleBufferFactory;

/**
 * 	An audio processor that converts the bit-depth of an audio stream.
 * 	The algorithm used to provide the conversion is enumerated within
 * 	the {@link BitDepthConversionAlgorithm} enum, a public inner class
 * 	within this class. The class supports chainable and direct processing,
 * 	like all audio processors should.
 * 	<p>
 * 	To use the class on an audio stream, use something like the following:
 * 	<p>
 * 	<code><pre>
 * 		BitDepthConverter bdc = new BitDepthConverter(
 * 			audioStream,
 * 			BitDepthConversionAlgorithm.NEAREST,
 * 			new AudioFormat( 8, 44.1, 1 ) );
 * 	</pre></code>
 * 	<p>
 * 	The constructors take an output format which must match the audio format
 * 	of the incoming stream in all respects other than the bit-depth. If the
 * 	input and output formats differ, an {@link IllegalArgumentException} will
 * 	be thrown. If the input and output formats are identical in every respect,
 * 	the processor does nothing.
 * 	<p>
 * 	For the NEAREST algorithm, the conversion of the bit-depth is
 * 	mainly provided through the {@link SampleBuffer} class.
 *
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	@created 18 Jun 2012
 */
public class BitDepthConverter extends AudioProcessor
{
	/**
	 * 	An enumerator of the different bit-depth conversion algorithms
	 * 	available.
	 *
	 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *	@created 18 Jun 2012
	 */
	public enum BitDepthConversionAlgorithm
	{
		/**
		 * 	Performs a basic nearest value rounding bit-depth conversion. It
		 * 	does this by utilising the sample buffer conversion routines.
		 */
		NEAREST
		{
			@Override
            public SampleChunk process( final SampleChunk s, final AudioFormat output )
            {
				final SampleBuffer sbin = s.getSampleBuffer();
				final SampleBuffer sbout = SampleBufferFactory.createSampleBuffer(
						output, sbin.size() );
				sbout.setFormat( output );

				// The sample buffer will do the conversion
				for( int i = 0; i < sbin.size(); i++ )
					sbout.set( i, sbin.get(i) );

				return sbout.getSampleChunk();
            }
		};

		/**
		 * 	Process a sample chunk and output a sample chunk in the given
		 * 	output format.
		 *
		 *  @param s The input sample chunk
		 *  @param output The output format
		 *  @return A resampled sample chunk.
		 */
		public abstract SampleChunk process( SampleChunk s, AudioFormat output );
	}

	/**
	 * Bit depth conversion defaults to
	 * {@link BitDepthConversionAlgorithm#NEAREST}
	 */
	private BitDepthConversionAlgorithm bitDepthConverter =
		BitDepthConversionAlgorithm.NEAREST;

	/** The output format to which sample chunks will be converted */
	private AudioFormat outputFormat = null;

	/**
	 * 	Default constructor that takes the input conversion
	 *  @param converter The converter to use
	 *  @param outputFormat The output format to convert to
	 */
	public BitDepthConverter( final BitDepthConversionAlgorithm converter,
			final AudioFormat outputFormat )
    {
		this.bitDepthConverter = converter;
		this.outputFormat = outputFormat;
		this.setFormat( outputFormat );
    }

	/**
	 * 	Chainable constructor.
	 *
	 *  @param as The audio stream to process
	 *  @param converter The converter to use
	 *  @param outputFormat The output format to convert to
	 */
	public BitDepthConverter( final AudioStream as, final BitDepthConversionAlgorithm converter,
			final AudioFormat outputFormat )
	{
		super( as );
		this.bitDepthConverter = converter;
		this.outputFormat = outputFormat;
		this.setFormat( outputFormat );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process( final SampleChunk sample ) throws Exception
	{
		if( sample.getFormat().getSampleRateKHz() != this.outputFormat.getSampleRateKHz() )
			throw new IllegalArgumentException( "The sample rate of the " +
					"output format is not the same as the sample chunk. Use a " +
					"sample rate converter first before using the bit depth" +
					"converter." );

		if( sample.getFormat().getNumChannels() != this.outputFormat.getNumChannels() )
			throw new IllegalArgumentException( "The number of channels in the " +
					"output format is not the same as the sample chunk. Use a " +
					"channel converter first before using the bit-depth " +
					"converter." );

		if( sample.getFormat().getNBits() == this.outputFormat.getNBits() )
			return sample;

		final SampleChunk sc = this.bitDepthConverter.process( sample, this.outputFormat );
		sc.setStartTimecode( sample.getStartTimecode() );
		return sc;
	}
}
