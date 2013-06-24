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
import org.openimaj.math.util.Interpolation;

/**
 * 	A sample rate conversion audio processing class. There is an enumerator
 * 	within the class that is publically available for determining the
 * 	algorithm for sample rate conversion. This defaults to
 * 	{@link SampleRateConversionAlgorithm#LINEAR_INTERPOLATION}.
 * 	<p>
 * 	To use the class, instantiate using the default constructor or the
 * 	chainable constructor. Both of these constructors take the algorithm for
 * 	sample rate conversion as well as the output format. The output format
 * 	must have the same number of bits and same number of channels as the
 * 	expected input otherwise the {@link #process(SampleChunk)} method
 * 	will throw {@link IllegalArgumentException}. The input format for the samples
 * 	is expected to be provided as part of the {@link SampleChunk}.
 * 	<p>
 * 	The class itself checks whether the output format and the input format
 * 	are the same (in which case the sample does not need to be resampled).
 * 	That means the algorithm implementation does not need to do this.
 *
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *
 *	@created 18 Jun 2012
 */
public class SampleRateConverter extends AudioProcessor
{
	/**
	 * 	An enumerator of the different sample rate conversion algorithms
	 * 	available in this sample rate converter.
	 *
	 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *
	 *	@created 18 Jun 2012
	 */
	public enum SampleRateConversionAlgorithm
	{
		/**
		 * 	Performs linear interpolation between samples where the sample rate
		 * 	in the output format is greater than the sample rate of the input
		 * 	format. If the sample rate is less, then the nearest value
		 * 	of the input samples is used.
		 */
		LINEAR_INTERPOLATION
		{
			@Override
            public SampleChunk process( final SampleChunk s, final AudioFormat output)
            {
				final AudioFormat input = s.getFormat();

				// Check to see if the input and output are the same
				if( input.getSampleRateKHz() == output.getSampleRateKHz() )
					return s;

				// Work out the size of the output sample chunk
				final double scalar = input.getSampleRateKHz() / output.getSampleRateKHz();
				final SampleBuffer sbin = s.getSampleBuffer();
				final double size = sbin.size() / scalar;

				if( this.sbout == null || this.sbout.size() != (int)size )
				{
					this.sbout = SampleBufferFactory.createSampleBuffer(
						output, (int)size );
					this.sbout.setFormat( output );
				}

				// If the input format has a greater sample rate than the
				// output format - down sampling (scalar > 1)
				if( scalar > 1 )
				{
					for( int i = 0; i < this.sbout.size(); i++ )
						this.sbout.set( i, sbin.get( (int)(i * scalar) ) );
					return this.sbout.getSampleChunk();
				}
				// If the input format has a sample rate less than that
				// of the output - up sampling (scalar < 1)
				else
				{
					// Linear interpolate each sample value
					for( int i = 0; i < this.sbout.size()-1; i++ )
					{
						final int inputSampleX = (int)(i * scalar);
						this.sbout.set( i, Interpolation.lerp( (float)(i*scalar),
								inputSampleX, sbin.get(inputSampleX),
								inputSampleX+1, sbin.get(inputSampleX+1) ) );
					}
					this.sbout.set( this.sbout.size()-1, sbin.get(sbin.size()-1) );
					return this.sbout.getSampleChunk();
				}
            }
		};

		protected SampleBuffer sbout = null;

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
	 * Sample rate conversion defaults to
	 * {@link SampleRateConversionAlgorithm#LINEAR_INTERPOLATION}
	 */
	private SampleRateConversionAlgorithm sampleConverter =
		SampleRateConversionAlgorithm.LINEAR_INTERPOLATION;

	/** The output format to which sample chunks will be converted */
	private AudioFormat outputFormat = null;

	/**
	 * 	Default constructor that takes the input conversion
	 *  @param converter The converter to use
	 *  @param outputFormat The output format to convert to
	 */
	public SampleRateConverter( final SampleRateConversionAlgorithm converter,
			final AudioFormat outputFormat )
    {
		this.sampleConverter = converter;
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
	public SampleRateConverter( final AudioStream as, final SampleRateConversionAlgorithm converter,
			final AudioFormat outputFormat )
	{
		super( as );
		this.sampleConverter = converter;
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
		if( sample.getFormat().getNBits() != this.outputFormat.getNBits() )
			throw new IllegalArgumentException( "The number of bits in the " +
					"output format is not the same as the sample chunk. Use a " +
					"resampling conversion first before using the sample-rate " +
					"converter." );

		if( sample.getFormat().getNumChannels() != this.outputFormat.getNumChannels() )
			throw new IllegalArgumentException( "The number of channels in the " +
					"output format is not the same as the sample chunk. Use a " +
					"channel converter first before using the sample-rate " +
					"converter." );

		if( sample.getFormat().getSampleRateKHz() == this.outputFormat.getSampleRateKHz() )
			return sample;

		final SampleChunk sc = this.sampleConverter.process( sample, this.outputFormat );
		sc.setStartTimecode( sample.getStartTimecode() );
		return sc;
	}
}
