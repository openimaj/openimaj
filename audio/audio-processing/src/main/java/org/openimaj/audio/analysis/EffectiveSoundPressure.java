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
package org.openimaj.audio.analysis;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.FixedSizeSampleAudioProcessor;

/**
 * 	Calculate the effective sound pressure by calculating the RMS of samples over
 * 	a temporal window. This will sum across all channels in the sample chunk.
 * 
 *	@author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class EffectiveSoundPressure extends FixedSizeSampleAudioProcessor
{
	private double rms = 0;

	/**
	 * Default constructor
	 */
	public EffectiveSoundPressure()
	{
		super( 1 );
	}

	/**
	 * 	Contstructor for ad-hoc processing. Note that the window and overlap
	 * 	size is given in samples (not in milliseconds as the other constructor).
	 * 
	 * 	@param windowSizeSamples Size of the processing window in samples
	 * 	@param overlapSamples The overlap of the windows in samples
	 */
	public EffectiveSoundPressure( final int windowSizeSamples, final int overlapSamples )
	{
		super( windowSizeSamples );
		this.setWindowStep( overlapSamples );
	}

	/**
	 * 	Construct with given stream and window parameters. Note that the window
	 * 	parameters are given in milliseconds and converted into a number of
	 * 	samples by the method.
	 * 
	 * 	@param stream The audio stream
	 * 	@param windowSizeMillis The window size in milliseconds
	 * 	@param overlapMillis The overlap between windows in milliseconds
	 */
	public EffectiveSoundPressure( final AudioStream stream, final int windowSizeMillis,
			final int overlapMillis )
	{
		super( stream, (int) (stream.getFormat().getSampleRateKHz()
				* windowSizeMillis * stream.getFormat().getNumChannels()) );
		this.setWindowStep( (int) (stream.getFormat().getSampleRateKHz() * 
				overlapMillis * stream.getFormat().getNumChannels()) );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.processor.FixedSizeSampleAudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process( final SampleChunk sample ) throws Exception
	{
		long accum = 0;
		final int size;

		switch (sample.getFormat().getNBits())
		{
			case 16:
			{
				final ShortBuffer b = sample.getSamplesAsByteBuffer().asShortBuffer();
				size = b.limit();
				for( int x = 0; x < size; x++ )
					accum += b.get( x ) * b.get( x );
				break;
			}
			case 8:
			{
				final ByteBuffer b = sample.getSamplesAsByteBuffer();
				size = b.limit();
				for( int x = 0; x < size; x++ )
					accum += b.get( x ) * b.get( x );
				break;
			}
			default:
				throw new Exception( "Unsupported Format" );
		}

		this.rms = Math.sqrt( (double) accum / (double) size );

		return sample;
	}

	/**
	 * @return The effective sound pressure
	 */
	public double getEffectiveSoundPressure()
	{
		return this.rms;
	}
}
