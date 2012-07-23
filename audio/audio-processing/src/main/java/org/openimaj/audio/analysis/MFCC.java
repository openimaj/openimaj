/**
 * 
 */
package org.openimaj.audio.analysis;

import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.filters.HanningAudioProcessor;

/**
 *	MFCC coefficient calculator.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 23 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class MFCC
{
	private FourierTransform fft = new FourierTransform();
	private HanningAudioProcessor hanning = new HanningAudioProcessor( 1024 );
	private double sum = -1;
	
	/**
	 *	Default constructor
	 */
	public MFCC()
    {
    }
	
	/**
	 * (1) normalized power fft with hanning window function<br>
	 * (2) convert to Mel scale by applying a mel filter bank<br>
	 * (3) Conversion to db<br>
	 * (4) finally a DCT is performed to get the mfcc<br>
	 * 	
	 * 	@param samples The sample chunk to generate MFCC for
	 *	@return The MFCC coefficients for each channel
	 */
	public float[][] generateMFCC( SampleChunk samples )
	{
		if( sum == -1 )
			sum = hanning.getWindowSum( samples );
		
		// Hanning window the samples
		SampleChunk windowedSamples = hanning.process( samples );
		
		// FFT the windowed samples
		fft.process( windowedSamples );
		float[][] lastFFT = fft.getLastFFT();
		
		// Normalise Power FFT
		for( int c = 0; c < lastFFT.length; c++ )
		{
			for( int i = 0; i < lastFFT[c].length; i+=2 )
			{
				float re = (float)(lastFFT[c][i] / sum * 2);
				float im = (float)(lastFFT[c][i+1] / sum * 2);
				lastFFT[c][i] = re*re+im*im;
			}
		}
		
		// Apply Mel-filters
		
		// Convert to dB
		
		// DCT to get MFCC
		
		return new float[0][0];
	}
}
