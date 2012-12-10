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

import java.util.Arrays;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.analysis.MFCC.MFCCFeatureVector;
import org.openimaj.audio.filters.HanningAudioProcessor;
import org.openimaj.audio.filters.MelFilterBank;
import org.openimaj.audio.samples.FloatSampleBuffer;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FloatFV;
import org.openimaj.util.array.ArrayUtils;

/**
 *	MFCC coefficient calculator.
 * 	<p>
 * 	The MFCCs are calculated using the following procedure:
 * 	<ol>
 * 		<li> Apply Hanning window scaling to samples </li>
 * 		<li> Calculate a normalized power FFT </li>
 * 		<li> Apply a Mel filter bank to the FFT results </li>
 * 		<li> Convert to db </li>
 * 		<li> Apply a DCT to get the MFCCs </li>
 * 	</ol>
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 23 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class MFCC implements FeatureExtractor<MFCCFeatureVector, SampleChunk>
{
	/**
	 * 	A feature vector class for MFCCs
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 6 Dec 2012
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static class MFCCFeatureVector extends FloatFV
	{
		/** */
		private static final long serialVersionUID = 1L;
		
		/** The number of channels in the fv */
		public int nChans;

		/**
		 *	Construct a feature vector from the mfccs 
		 *	@param mfcc The mfccs [chan][coeff]
		 */
		public MFCCFeatureVector( final float[][] mfcc )
		{
			super( ArrayUtils.reshape( mfcc ) );
			this.nChans = mfcc.length;
		}
	}
	
	/** Whether to debug the class */
	private static final boolean DEBUG = false;
	
	/** The Fourier transform processor */
	private final FourierTransform fft = new FourierTransform();
	
	/** The Hanning window processor */
	private final HanningAudioProcessor hanning = new HanningAudioProcessor( 1024 );
	
	/** The sum of the Hanning window processor */
	private double sum = -1;
	
	/** The number of coefficients to get */
	private final int nCoeffs = 20;
	
	/**
	 *	Default constructor
	 */
	public MFCC()
    {
    }
	
	/**
	 * 	Calculate the MFCCs for a given sample chunk.
	 * 
	 * 	<p>
	 * 	The MFCCs are calculated using the following procedure:
	 * 	<ol>
	 * 		<li> Apply Hanning window scaling to samples </li>
	 * 		<li> Calculate a normalized power FFT </li>
	 * 		<li> Apply a Mel filter bank to the FFT results </li>
	 * 		<li> Convert to db </li>
	 * 		<li> Apply a DCT to get the MFCCs </li>
	 * 	</ol>
	 * 	
	 * 	@param samples The sample chunk to generate MFCC for
	 *	@return The MFCC coefficients for each channel
	 */
	public float[][] calculateMFCC( final SampleChunk samples )
	{
		return this.calculateMFCC( ArrayUtils.normalise( 
				samples.getSampleBuffer().asDoubleArray() ), 
				samples.getFormat() );
	}
	
	/**
	 * 	Calculate the MFCCs for a normalised (0-1) set of multiplexed samples.
	 * 
	 * 	<p>
	 * 	The MFCCs are calculated using the following procedure:
	 * 	<ol>
	 * 		<li> Apply Hanning window scaling to samples </li>
	 * 		<li> Calculate a normalized power FFT </li>
	 * 		<li> Apply a Mel filter bank to the FFT results </li>
	 * 		<li> Convert to db </li>
	 * 		<li> Apply a DCT to get the MFCCs </li>
	 * 	</ol>
	 * 	
	 * 	@param samples The sample chunk to generate MFCC for
	 * 	@param format The format of the samples 
	 *	@return The MFCC coefficients for each channel
	 */
	public float[][] calculateMFCC( final double[] samples, final AudioFormat format )
	{
		// Lazily calculate the hanning window sum
		if( this.sum == -1 )
			this.sum = this.hanning.getWindowSum( samples.length, format.getNumChannels() );
		
		// Create a non-scaling buffer
		final FloatSampleBuffer sb = new FloatSampleBuffer( samples, format );
		
		// Convert to db power
		sb.multiply( Math.pow( 10, 96/20 ) );

		// Weight the samples with a Hanning window
		final SampleBuffer windowedSamples = this.hanning.process( sb );
		
		// FFT the windowed samples
		this.fft.process( windowedSamples );
		final float[][] lastFFT = this.fft.getLastFFT();

		if( MFCC.DEBUG )
		{
			System.out.println( "FFT: "+Arrays.deepToString( lastFFT ) );
			System.out.println( "Window function sum: "+this.sum );
		}
		
		// Normalise Power FFT
		final float[][] powerSpectrum = this.fft.getNormalisedMagnitudes( 2f*(float)this.sum );
		
		if( MFCC.DEBUG )
		{
			System.out.println( "Size of power spectrum: "+powerSpectrum[0].length );		
			System.out.println( "PowerSpectrum: "+Arrays.deepToString( powerSpectrum ) );
		}
		
		// Apply Mel-filters
		final int nFilters = 40;
		final float[][] melPowerSpectrum = new MelFilterBank( nFilters, 20, 16000 )
							.process( powerSpectrum, format );

		if( MFCC.DEBUG )
			System.out.println( "MelPowerSpectrum: "+Arrays.deepToString( melPowerSpectrum ) );
		
		// Convert to dB
		for( int c = 0; c < melPowerSpectrum.length; c++ )
			for( int i = 0; i < melPowerSpectrum[c].length; i++ )
				melPowerSpectrum[c][i] = (float)(10 * Math.log10( 
						Math.max( melPowerSpectrum[c][i], 1 ) ) );

		if( MFCC.DEBUG )
		{
			System.out.println( "MelPowerSpectrum(db): "+Arrays.deepToString( melPowerSpectrum ) );
			System.out.println( "Size of Mel Power Cepstrum: "+melPowerSpectrum[0].length );
		}
		
		// DCT to get MFCC
//		FloatDCT_1D dct = new FloatDCT_1D( melPowerSpectrum[0].length );
//		for( int c = 0; c < melPowerSpectrum.length; c++ )
//			coeffs[c] = dct.forward( melPowerSpectrum[c], false );

	    final double k = Math.PI/nFilters;
	    final double w1 = 1.0/( Math.sqrt( nFilters ) );
	    final double w2 = Math.sqrt( 2.0 / nFilters);

		final float[][] coeffs = new float[melPowerSpectrum.length][this.nCoeffs];
		for( int c = 0; c < melPowerSpectrum.length; c++ )
			for( int cc = 0; cc < this.nCoeffs; cc++ )
				for( int f = 0; f < melPowerSpectrum[c].length; f++ )
					coeffs[c][cc] += melPowerSpectrum[c][f] *
							(cc==0?w1:w2) * Math.cos( k*cc * ( f + 0.5d ) );
						
		
		if( MFCC.DEBUG )
			System.out.println( "Number of coefficients: "+coeffs[0].length );
		
		return coeffs;
	}

	@Override
	public MFCCFeatureVector extractFeature( final SampleChunk object )
	{
		return new MFCCFeatureVector( this.calculateMFCC( object ) );
	}
}
