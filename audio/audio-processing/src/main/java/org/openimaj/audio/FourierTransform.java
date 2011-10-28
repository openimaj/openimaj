/**
 * 
 */
package org.openimaj.audio;

import java.nio.ShortBuffer;

import org.openimaj.audio.processor.AudioProcessor;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

/**
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
		FloatFFT_1D fft = new FloatFFT_1D( sample.getNumberOfSamples() );
		
		ShortBuffer sb = sample.getSamplesAsByteBuffer().asShortBuffer();
		
		lastFFT = new float[sample.getNumberOfSamples()*2];
		
		for( int x = 0; x < sample.getNumberOfSamples(); x++ )
			lastFFT[x*2] = sb.get(x)/32767f+1f;
		
		fft.complexForward( lastFFT );
		
	    return sample;
    }
	
	public float[] getLastFFT()
	{
		return this.lastFFT;
	}
}
