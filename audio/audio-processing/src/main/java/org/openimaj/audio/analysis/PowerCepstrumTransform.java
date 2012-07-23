/**
 * 
 */
package org.openimaj.audio.analysis;

import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.AudioProcessor;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

/**
 *	An implementation of the power cepstrum of an audio signal. The
 *	power cepstrum of an audio signal is the squared magnitude of the Fourier 
 *	transform of the logarithm of the squared magnitude of the Fourier transform 
 *	of a signal. Yeah, I know.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 18 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class PowerCepstrumTransform extends AudioProcessor
{
	/** The last generated cepstrum */
	private float[][] lastCepstrum = null;

	@Override
    public SampleChunk process( SampleChunk sample ) throws Exception
    {
		FourierTransform fft = new FourierTransform();
		
		// Working backwards...
		// ... the FFT of a signal...
		fft.process( sample );
		float[][] fftCoeffs = fft.getLastFFT();
		
		// ...the logarithm of the squared magnitude...
		float logMags[][] = new float[fftCoeffs.length][];
		for( int c = 0; c < fftCoeffs.length; c++ )
		{
			logMags[c] = new float[fftCoeffs[c].length/4];
			for( int i = 0; i < fftCoeffs[c].length/4; i++ )
			{
				// Calculate magnitude
				float re = fftCoeffs[c][i*2];
				float im = fftCoeffs[c][i*2+1];
				float mag = (float)Math.log(Math.sqrt( re*re + im*im )+1);
				
				// Square
				mag *= mag;
				
				// Logarithm
				float logMag = (float)Math.log( mag );
				
				// Store
				logMags[c][i] = logMag;
			}
		}
		
		// ... the Fast Fourier (of the log-squared-mags)
		this.lastCepstrum  = new float[ logMags.length ][];
		FloatFFT_1D fft2 = new FloatFFT_1D( logMags[0].length/4 );
		for( int c = 0; c < logMags.length; c++ )
		{
			fft2.complexForward( logMags[c] );
			
			this.lastCepstrum[c] = new float[ logMags[c].length/4 ];
			
			// ...the squared magnitude of...
			for( int i = 0; i < logMags[c].length/4; i++ )
			{
				// Calculate magnitude
				float re = logMags[c][i*2];
				float im = logMags[c][i*2+1];
				float mag = (float)Math.log(Math.sqrt( re*re + im*im )+1);
				
				// Square
				mag *= mag;
				
				this.lastCepstrum[c][i] = mag;
			}
		}
		
	    return sample;
    }
	
	/**
	 * 	Returns the last generated cepstrum
	 *	@return The last generated cepstrum
	 */
	public float[][] getLastCepstrum()
	{
		return this.lastCepstrum;
	}
}
