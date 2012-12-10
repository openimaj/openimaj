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
	public Preemphasiser( final double factor )
	{
		this.factor = factor;
	}
	
	/**
	 * 	Chainiable constructor
	 *	@param as The stream to chain to
	 */
	public Preemphasiser( final AudioStream as )
	{
		super( as );
	}
	
	/**
	 * 	Chainable constructor that takes the emphasis factor.
	 *	@param as The stream to chain to
	 *	@param factor The emphasis factor
	 */
	public Preemphasiser( final AudioStream as, final double factor )
	{
		this( as );
		this.factor = factor;
	}
	
	/** 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process( final SampleChunk sample ) throws Exception
	{
		final SampleBuffer sb = sample.getSampleBuffer();
		final int nc = sb.getFormat().getNumChannels();
		for( int c = 0; c < nc; c++ )
		{
			float previous = 0;
			for( int s = 1; s < sb.size()/nc; s++ )
			{
				final float v = sb.get(s*c);
				sb.set( s*c, (float)(v - this.factor * previous) );
				previous = v;
			}
		}
		
		return sample;
	}
}
