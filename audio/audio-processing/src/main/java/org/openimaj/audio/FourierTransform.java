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
package org.openimaj.audio;

import java.nio.ShortBuffer;

import org.openimaj.audio.processor.AudioProcessor;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

/**
 * 	Perform an FFT on an audio signal. If the sample chunks have more than one
 * 	channel, only the first channel will be used.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 28 Oct 2011
 */
public class FourierTransform extends AudioProcessor
{
	private float[] lastFFT = null;
	
	@Override
    public SampleChunk process( SampleChunk sample )
    {		
		ShortBuffer sb = sample.getSamplesAsByteBuffer().asShortBuffer();
		
		// We only use the first channel
		final int nChans = sample.getFormat().getNumChannels();
		lastFFT = new float[sample.getNumberOfSamples()/nChans*2];
		
		// Fill the FFT input with values -0.5 to 0.5 (for signed) 0 to 1 for unsigned
		final float fftscale = (float)Math.pow( 2, sample.getFormat().getNBits() );
		for( int x = 0; x < sample.getNumberOfSamples()/nChans; x++ )
			lastFFT[x*2] = sb.get( x*nChans )/fftscale;
		
		FloatFFT_1D fft = new FloatFFT_1D( sample.getNumberOfSamples()/nChans );
		fft.complexForward( lastFFT );
		
	    return sample;
    }
	
	/**
	 * @return The fft of the last processed window 
	 */
	public float[] getLastFFT()
	{
		return this.lastFFT;
	}
}
