/**
 * 
 */
package org.openimaj.audio.filters;

import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.AudioProcessor;
import org.openimaj.audio.samples.SampleBuffer;

/**
 *	A simple preemphasiser that applies a high-pass filter to the the audio
 *	for speech signals. It's basically a really simple comb filter.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 13 Aug 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class Preemphasiser extends AudioProcessor
{
	/** Default preemphasis factor */
	private double factor = 0.97;
	
	/**
	 * 	Default constructor
	 */
	public Preemphasiser()
    {
    }
	
	/**
	 * 	Constructor that takes the emphasis factor.
	 *	@param factor The emphasis factor
	 */
	public Preemphasiser( double factor )
	{
		this.factor = factor;
	}
	
	/**
	 * 	Chainiable constructor
	 *	@param as The stream to chain to
	 */
	public Preemphasiser( AudioStream as )
	{
		super( as );
	}
	
	/**
	 * 	Chainable constructor that takes the emphasis factor.
	 *	@param as The stream to chain to
	 *	@param factor The emphasis factor
	 */
	public Preemphasiser( AudioStream as, double factor )
	{
		this( as );
		this.factor = factor;
	}
	
	/** 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process( SampleChunk sample ) throws Exception
	{
		SampleBuffer sb = sample.getSampleBuffer();
		int nc = sb.getFormat().getNumChannels();
		for( int c = 0; c < nc; c++ )
		{
			float previous = 0;
			for( int s = 1; s < sb.size()/nc; s++ )
			{
				float v = sb.get(s*c);
				sb.set( s*c, (float)(v - factor * previous) );
				previous = v;
			}
		}
		
		return sample;
	}
}
