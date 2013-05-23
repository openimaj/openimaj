/**
 *
 */
package org.openimaj.audio.filters;

import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.FixedSizeSampleAudioProcessor;
import org.openimaj.audio.samples.SampleBuffer;

/**
 *	FIR filter is a multiplication of the frequency domain of a signal with the frequency
 *	domain of a filter. That corresponds to a convolution in the time domain; that is,
 *	a weighted sum of the previous input.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 22 May 2013
 */
public abstract class FIRFilter extends FixedSizeSampleAudioProcessor
{
	/** The coefficients for this filter */
	private final double[] coefficients;

	/**
	 *	@param stream
	 */
	public FIRFilter( final AudioStream stream )
	{
		super( stream, 4, 3 );

		this.coefficients = this.getCoefficients();
		super.setWindowSize( this.coefficients.length );
		super.setWindowStep( this.coefficients.length-1 );
	}

	/**
	 * 	Returns the coefficients for the particular filter
	 *	@return coefficients
	 */
	public abstract double[] getCoefficients();

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.processor.FixedSizeSampleAudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process( final SampleChunk sample ) throws Exception
	{
		final SampleBuffer sb = sample.getSampleBuffer();

		for( int c = 0; c < sample.getFormat().getNumChannels(); c++ )
		{
			float acc = 0;
			for( int i = 0; i < this.coefficients.length; i++ )
				acc += sb.get(i) * this.coefficients[i];
			sb.set( 0, acc );
		}

		return sample;
	}
}
