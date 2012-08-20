/**
 * 
 */
package org.openimaj.audio.analysis;

import java.util.Arrays;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.filters.HanningAudioProcessor;
import org.openimaj.audio.filters.MelFilterBank;
import org.openimaj.audio.samples.FloatSampleBuffer;
import org.openimaj.audio.samples.SampleBuffer;
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
public class MFCC
{
	/** The Fourier transform processor */
	private FourierTransform fft = new FourierTransform();
	
	/** The Hanning window processor */
	private HanningAudioProcessor hanning = new HanningAudioProcessor( 1024 );
	
	/** The sum of the Hanning window processor */
	private double sum = -1;
	
	/** The number of coefficients to get */
	private int nCoeffs = 20;
	
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
	public float[][] calculateMFCC( SampleChunk samples )
	{
		return calculateMFCC( ArrayUtils.normalise( 
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
		if( sum == -1 )
			sum = hanning.getWindowSum( samples.length, format.getNumChannels() );
		
		// Create a non-scaling buffer
		FloatSampleBuffer sb = new FloatSampleBuffer( samples, format );
		
		// Convert to db power
		sb.multiply( Math.pow( 10, 96/20 ) );

		// Weight the samples with a Hanning window
		SampleBuffer windowedSamples = hanning.process( sb );
		
		// FFT the windowed samples
		fft.process( windowedSamples );
		float[][] lastFFT = fft.getLastFFT();

		System.out.println( "FFT: "+Arrays.deepToString( lastFFT ) );
		System.out.println( "Window function sum: "+sum );
		
		// Normalise Power FFT
		float[][] powerSpectrum = fft.getNormalisedMagnitudes( 2f*(float)sum );
		
		System.out.println( "Size of power spectrum: "+powerSpectrum[0].length );		
		System.out.println( "PowerSpectrum: "+Arrays.deepToString( powerSpectrum ) );
		
		// Apply Mel-filters
		int nFilters = 40;
		float[][] melPowerSpectrum = new MelFilterBank( nFilters, 20, 16000 )
							.process( powerSpectrum, format );

		System.out.println( "MelPowerSpectrum: "+Arrays.deepToString( melPowerSpectrum ) );
		
		// Convert to dB
		for( int c = 0; c < melPowerSpectrum.length; c++ )
			for( int i = 0; i < melPowerSpectrum[c].length; i++ )
				melPowerSpectrum[c][i] = (float)(10 * Math.log10( 
						Math.max( melPowerSpectrum[c][i], 1 ) ) );

		System.out.println( "MelPowerSpectrum(db): "+Arrays.deepToString( melPowerSpectrum ) );
		System.out.println( "Size of Mel Power Cepstrum: "+melPowerSpectrum[0].length );
		
		// DCT to get MFCC
//		FloatDCT_1D dct = new FloatDCT_1D( melPowerSpectrum[0].length );
//		for( int c = 0; c < melPowerSpectrum.length; c++ )
//			coeffs[c] = dct.forward( melPowerSpectrum[c], false );

	    double k = Math.PI/nFilters;
	    double w1 = 1.0/( Math.sqrt( nFilters ) );
	    double w2 = Math.sqrt( 2.0 / nFilters);

		float[][] coeffs = new float[melPowerSpectrum.length][nCoeffs];
		for( int c = 0; c < melPowerSpectrum.length; c++ )
			for( int cc = 0; cc < nCoeffs; cc++ )
				for( int f = 0; f < melPowerSpectrum[c].length; f++ )
					coeffs[c][cc] += melPowerSpectrum[c][f] *
							(cc==0?w1:w2) * Math.cos( k*cc * ( f + 0.5d ) );
						
		
		System.out.println( "Number of coefficients: "+coeffs[0].length );
		return coeffs;
	}
}
