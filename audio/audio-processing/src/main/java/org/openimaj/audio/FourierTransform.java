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
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
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
	
	public float[] getLastFFT()
	{
		return this.lastFFT;
	}
}
