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

/**
 *	Applies a Hamming function over a window of samples.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 4 Feb 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class HammingAudioProcessor extends HanningAudioProcessor
{
	/**
	 * 	Constructor that takes the size of the window required.
	 *	@param sizeRequired The size of the window in samples
	 */
	public HammingAudioProcessor( final int sizeRequired )
	{
		super( sizeRequired );
	}
	
	/**
	 * 	Chainable constructor that takes the audio stream to chain to
	 * 	and the size of the window required.
	 *	@param stream The audio to chain to
	 *	@param sizeRequired the size of the window in samples
	 */
	public HammingAudioProcessor( final AudioStream stream, final int sizeRequired )
	{
		super( stream, sizeRequired );
	}
	
	/**
	 * 	Constructor that takes the size of the window and the number of samples
	 * 	overlap.
	 * 
	 *	@param nSamplesInWindow Samples in window
	 *	@param nSamplesOverlap Samples in window overlap
	 */
	public HammingAudioProcessor( final int nSamplesInWindow, final int nSamplesOverlap )
	{
		super( nSamplesInWindow, nSamplesOverlap );
	}

	/**
	 * 	Chainable constructor that takes the size of the window and 
	 * 	the number of samples overlap.
	 * 
	 * 	@param as The chained audio stream
	 *	@param nSamplesInWindow Samples in window
	 *	@param nSamplesOverlap Samples in window overlap
	 */
	public HammingAudioProcessor( final AudioStream as, 
			final int nSamplesInWindow, final int nSamplesOverlap )
	{
		super( as, nSamplesInWindow, nSamplesOverlap );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.filters.HanningAudioProcessor#generateWeightTableCache(int, int)
	 */
	@Override
	protected void generateWeightTableCache( final int length, final int nc )
	{
		final int ns = length;
		this.weightTable = new double[ length ];
		for( int n = 0; n < ns; n++ )
			for( int c = 0; c < nc; c++ )
				this.weightTable[n*nc+c] = 0.54-0.46*Math.cos((2*Math.PI*n)/ns);		
	}
}
