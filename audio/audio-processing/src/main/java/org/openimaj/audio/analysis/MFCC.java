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

import edu.emory.mathcs.jtransforms.dct.FloatDCT_1D;

/**
 *	MFCC coefficient calculator.
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
		
		// Convert to power
		sb.multiply( Math.pow( 10, 96/20 ) );

		// Hanning window the samples
		SampleBuffer windowedSamples = hanning.process( sb );
		
		// FFT the windowed samples
		fft.process( windowedSamples );
		float[][] lastFFT = fft.getLastFFT();

		System.out.println( "FFT: "+Arrays.deepToString( lastFFT ) );
		System.out.println( "Window function sum: "+sum );
		
		// Normalise Power FFT
		float[][] powerSpectrum = fft.getPowerMagnitudes();
//		new float[lastFFT.length][];
//		for( int c = 0; c < lastFFT.length; c++ )
//		{
//			powerSpectrum[c] = new float[lastFFT[c].length/4];
//			for( int i = 0; i < lastFFT[c].length/2; i += 2 )
//			{
//				float re = (float)(lastFFT[c][i] / sum * 2);
//				float im = (float)(lastFFT[c][i+1] / sum * 2);
//				powerSpectrum[c][i/2] = (re*re)+(im*im);
//			}
//		}

		System.out.println( "PowerSpectrum: "+Arrays.deepToString( powerSpectrum ) );
		
		// Apply Mel-filters
		float[][] melPowerSpectrum = new MelFilterBank( 40, 20, 16000 )
							.process( powerSpectrum, format );

		System.out.println( "MelPowerSpectrum: "+Arrays.deepToString( melPowerSpectrum ) );
		
		// Convert to dB
		for( int c = 0; c < melPowerSpectrum.length; c++ )
			for( int i = 0; i < melPowerSpectrum[c].length; i++ )
				melPowerSpectrum[c][i] = (float)(10 * Math.log10( 
						Math.max( melPowerSpectrum[c][i], 1 ) ) );

		System.out.println( "MelPowerSpectrum(db): "+Arrays.deepToString( melPowerSpectrum ) );

		// DCT to get MFCC
		FloatDCT_1D dct = new FloatDCT_1D( melPowerSpectrum[0].length );
		for( int c = 0; c < melPowerSpectrum.length; c++ )
			dct.forward( melPowerSpectrum[c], false );
		
		return melPowerSpectrum;
	}
}
