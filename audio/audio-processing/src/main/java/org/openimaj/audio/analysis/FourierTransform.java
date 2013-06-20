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
package org.openimaj.audio.analysis;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.AudioProcessor;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.audio.samples.SampleBufferFactory;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

/**
 * 	Perform an FFT on an audio signal. An FFT will be calculated for every
 * 	channel in the audio separately. Use {@link #getLastFFT()} to get the
 * 	last generated frequency domain calculation.
 * 	<p>
 * 	The class also includes an inverse transform function that takes a
 * 	frequency domain array (such as that delivered by {@link #getLastFFT()})
 * 	and returns a {@link SampleChunk}. The format of the output sample chunk
 * 	is determined by the given audio format.
 *
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	@created 28 Oct 2011
 */
public class FourierTransform extends AudioProcessor
{
	/** The last generated FFT */
	private float[][] lastFFT = null;

	/** The scaling factor to apply prior to the FFT */
	private float scalingFactor = 1;

	/** Whether to pad the input to the next power of 2 */
	private boolean padToNextPowerOf2 = true;

	/** Whether to divide the real return parts by the size of the input */
	private final boolean normalise = true;

	/**
	 * 	Default constructor for ad-hoc processing.
	 */
	public FourierTransform()
	{
	}

	/**
	 * 	Constructor for chaining.
	 *	@param as The stream to chain to
	 */
	public FourierTransform( final AudioStream as )
	{
		super( as );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
    public SampleChunk process( final SampleChunk sample )
    {
		// Get a sample buffer object for this data
		final SampleBuffer sb = sample.getSampleBuffer();
		return this.process( sb ).getSampleChunk();
    }

	/**
	 * 	Process the given sample buffer
	 *	@param sb The sample buffer
	 *	@return The sample buffer
	 */
	public SampleBuffer process( final SampleBuffer sb )
	{
		// The number of channels we need to process
		final int nChannels = sb.getFormat().getNumChannels();

		// Number of samples we'll need to process for each channel
		final int nSamplesPerChannel = sb.size() / nChannels;

		// The size of the FFT to generate
		final int sizeOfFFT = this.padToNextPowerOf2 ?
				this.nextPowerOf2( nSamplesPerChannel ) : nSamplesPerChannel;

		// The Fourier transformer we're going to use
		final FloatFFT_1D fft = new FloatFFT_1D( nSamplesPerChannel );

		// Creates an FFT for each of the channels in turn
		this.lastFFT = new float[nChannels][];
		for( int c = 0; c < nChannels; c++ )
		{
			// Twice the length to account for imaginary parts
			this.lastFFT[c] = new float[ sizeOfFFT*2 ];

			// Fill the array
			for( int x = 0; x < nSamplesPerChannel; x++ )
				this.lastFFT[c][x*2] = sb.get( x*nChannels+c ) * this.scalingFactor;

//			System.out.println( "FFT Input (channel "+c+"), length "+this.lastFFT[c].length+": " );
//			System.out.println( Arrays.toString( this.lastFFT[c] ));

			// Perform the FFT (using jTransforms)
			fft.complexForward( this.lastFFT[c] );

			if( this.normalise )
				this.normaliseReals( sizeOfFFT );

//			System.out.println( "FFT Output (channel "+c+"): " );
//			System.out.println( Arrays.toString( this.lastFFT[c] ));
		}

	    return sb;
    }

	/**
	 * 	Divides the real parts of the last FFT by the given size
	 *	@param size the divisor
	 */
	private void normaliseReals( final int size )
	{
		for( int c = 0; c < this.lastFFT.length; c++ )
			for( int i = 0; i < this.lastFFT[c].length; i +=2 )
				this.lastFFT[c][i] /= size;
	}

	/**
	 * 	Returns the next power of 2 superior to n.
	 *	@param n The value to find the next power of 2 above
	 *	@return The next power of 2
	 */
	private int nextPowerOf2( final int n )
	{
		return (int)Math.pow( 2, 32 - Integer.numberOfLeadingZeros(n - 1) );
	}

	/**
	 * 	Given some transformed audio data, will convert it back into
	 * 	a sample chunk. The number of channels given audio format
	 * 	must match the data that is provided in the transformedData array.
	 *
	 * 	@param format The required format for the output
	 *	@param transformedData The frequency domain data
	 *	@return A {@link SampleChunk}
	 */
	static public SampleChunk inverseTransform( final AudioFormat format,
			final float[][] transformedData )
	{
		// Check the data has something in it.
		if( transformedData == null || transformedData.length == 0 )
			throw new IllegalArgumentException( "No data in data chunk" );

		// Check that the transformed data has the same number of channels
		// as the data we've been given.
		if( transformedData.length != format.getNumChannels() )
			throw new IllegalArgumentException( "Number of channels in audio " +
					"format does not match given data." );

		// The number of channels
		final int nChannels = transformedData.length;

		// The Fourier transformer we're going to use
		final FloatFFT_1D fft = new FloatFFT_1D( transformedData[0].length/2 );

		// Create a sample buffer to put the time domain data into
		final SampleBuffer sb = SampleBufferFactory.createSampleBuffer( format,
				transformedData[0].length/2 *	nChannels );

		// Perform the inverse on each channel
		for( int channel = 0; channel < transformedData.length; channel++ )
		{
			// Convert frequency domain back to time domain
			fft.complexInverse( transformedData[channel], true );

			// Set the data in the buffer
			for( int x = 0; x < transformedData[channel].length/2; x++ )
				sb.set( x*nChannels+channel, transformedData[channel][x] );
		}

		// Return a new sample chunk
		return sb.getSampleChunk();
	}

	/**
	 * 	Get the last processed FFT frequency data.
	 * 	@return The fft of the last processed window
	 */
	public float[][] getLastFFT()
	{
		return this.lastFFT;
	}

	/**
	 * 	Returns the magnitudes of the last FFT data. The length of the
	 * 	returned array of magnitudes will be half the length of the FFT data
	 * 	(up to the Nyquist frequency).
	 *
	 *	@return The magnitudes of the last FFT data.
	 */
	public float[][] getMagnitudes()
	{
		final float[][] mags = new float[this.lastFFT.length][];
		for( int c = 0; c < this.lastFFT.length; c++ )
		{
			mags[c] = new float[ this.lastFFT[c].length/4 ];
			for( int i = 0; i < this.lastFFT[c].length/4; i++ )
			{
				final float re = this.lastFFT[c][i*2];
				final float im = this.lastFFT[c][i*2+1];
				mags[c][i] = (float)Math.sqrt( re*re + im*im );
			}
		}

		return mags;
	}

	/**
	 * 	Returns the power magnitudes of the last FFT data. The length of the
	 * 	returned array of magnitudes will be half the length of the FFT data
	 * 	(up to the Nyquist frequency). The power is calculated using:
	 * 	<p><code>10log10( real^2 + imaginary^2 )</code></p>
	 *
	 *	@return The magnitudes of the last FFT data.
	 */
	public float[][] getPowerMagnitudes()
	{
		final float[][] mags = new float[this.lastFFT.length][];
		for( int c = 0; c < this.lastFFT.length; c++ )
		{
			mags[c] = new float[ this.lastFFT[c].length/4 ];
			for( int i = 0; i < this.lastFFT[c].length/4; i++ )
			{
				final float re = this.lastFFT[c][i*2];
				final float im = this.lastFFT[c][i*2+1];
				mags[c][i] = 10f * (float)Math.log10( re*re + im*im );
			}
		}

		return mags;
	}

	/**
	 * 	Scales the real and imaginary parts by the scalar prior to
	 * 	calculating the (square) magnitude for normalising the outputs.
	 * 	Returns only those values up to the Nyquist frequency.
	 *
	 *	@param scalar The scalar
	 *	@return Normalised magnitudes.
	 */
	public float[][] getNormalisedMagnitudes( final float scalar )
	{
		final float[][] mags = new float[this.lastFFT.length][];
		for( int c = 0; c < this.lastFFT.length; c++ )
		{
			mags[c] = new float[ this.lastFFT[c].length/4 ];
			for( int i = 0; i < this.lastFFT[c].length/4; i++ )
			{
				final float re = this.lastFFT[c][i*2] * scalar;
				final float im = this.lastFFT[c][i*2+1] * scalar;
				mags[c][i] = re*re + im*im;
			}
		}

		return mags;
	}

	/**
	 * 	Returns just the real numbers from the last FFT. The result will include
	 * 	the symmetrical part.
	 *	@return The real numbers
	 */
	public float[][] getReals()
	{
		final float[][] reals = new float[this.lastFFT.length][];
		for( int c = 0; c < this.lastFFT.length; c++ )
		{
			reals[c] = new float[ this.lastFFT[c].length/2 ];
			for( int i = 0; i < this.lastFFT[c].length/2; i++ )
				reals[c][i] = this.lastFFT[c][i*2];
		}

		return reals;
	}

	/**
	 * 	Get the scaling factor in use.
	 *	@return The scaling factor.
	 */
	public float getScalingFactor()
	{
		return this.scalingFactor;
	}

	/**
	 * 	Set the scaling factor to use. This factor will be applied to signal
	 * 	data prior to performing the FFT. The default is, of course, 1.
	 *	@param scalingFactor The scaling factor to use.
	 */
	public void setScalingFactor( final float scalingFactor )
	{
		this.scalingFactor = scalingFactor;
	}

	/**
	 *	Returns whether the input will be padded to be the length
	 *	of the next higher power of 2.
	 *	@return TRUE if the input will be padded, FALSE otherwise.
	 */
	public boolean isPadToNextPowerOf2()
	{
		return this.padToNextPowerOf2;
	}

	/**
	 * 	Set whether to pad the input to the next power of 2.
	 *	@param padToNextPowerOf2 TRUE to pad the input, FALSE otherwise
	 */
	public void setPadToNextPowerOf2( final boolean padToNextPowerOf2 )
	{
		this.padToNextPowerOf2 = padToNextPowerOf2;
	}
}
