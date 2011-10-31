/**
 * 
 */
package org.openimaj.audio;

import java.nio.ShortBuffer;

import org.openimaj.audio.processor.FixedSizeSampleAudioProcessor;

/**
 * 	Applies a Hanning window on top of the audio signal
 * 	@see http://cnx.org/content/m0505/latest/
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 31 Oct 2011
 */
public abstract class HanningAudioProcessor extends FixedSizeSampleAudioProcessor
{
	/**
	 * 
	 *  @param stream
	 *  @param sizeRequired
	 */
	public HanningAudioProcessor( AudioStream stream, int sizeRequired )
    {
	    super( stream, sizeRequired );
    }

	/**
	 *  @inheritDoc
	 *  @see org.openimaj.audio.processor.FixedSizeSampleAudioProcessor#nextSampleChunk()
	 */
	@Override
	public SampleChunk nextSampleChunk()
	{
		SampleChunk sample = super.nextSampleChunk();
		ShortBuffer b = sample.getSamplesAsByteBuffer().asShortBuffer();
		final int nc = sample.getFormat().getNumChannels();
		final int ns = sample.getNumberOfSamples()/nc;
		for( int n = 0; n < ns; n++ )
			for( int c = 0; c < nc; c++ )
				b.put( n*nc+c, (short)(b.get(n*nc+c) * 0.5*(1-Math.cos((2*Math.PI*n)/ns))) );
		
		return sample;
	}
}
