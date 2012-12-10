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
    public SampleChunk process( final SampleChunk sample ) throws Exception
    {
		final FourierTransform fft = new FourierTransform();
		
		//
		// The squared magnitude of the Fourier transform of the logarithm 
		// of the squared magnitude of the Fourier transform of a signal...
		//
		// Working backwards...
		// ... the FFT of a signal...
		//
		fft.process( sample );
		final float[][] fftCoeffs = fft.getLastFFT();
		
		// ...the logarithm of the squared magnitude...
		final float logMags[][] = new float[fftCoeffs.length][];
		for( int c = 0; c < fftCoeffs.length; c++ )
		{
			logMags[c] = new float[fftCoeffs[c].length/4];
			for( int i = 0; i < fftCoeffs[c].length/4; i++ )
			{
				// Calculate magnitude
				final float re = fftCoeffs[c][i*2];
				final float im = fftCoeffs[c][i*2+1];
				float mag = (float)Math.log(Math.sqrt( re*re + im*im )+1);
				
				// Square
				mag *= mag;
				
				// Logarithm
				final float logMag = (float)Math.log( mag );
				
				// Store
				logMags[c][i] = logMag;
			}
		}
		
		// ... the Fast Fourier (of the log-squared-mags)
		this.lastCepstrum  = new float[ logMags.length ][];
		final FloatFFT_1D fft2 = new FloatFFT_1D( logMags[0].length/4 );
		for( int c = 0; c < logMags.length; c++ )
		{
			fft2.complexForward( logMags[c] );
			
			this.lastCepstrum[c] = new float[ logMags[c].length/4 ];
			
			// ...the squared magnitude of...
			for( int i = 0; i < logMags[c].length/4; i++ )
			{
				// Calculate magnitude
				final float re = logMags[c][i*2];
				final float im = logMags[c][i*2+1];
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
