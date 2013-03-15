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
