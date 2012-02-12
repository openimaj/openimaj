/**
 * 
 */
package org.openimaj.audio;

import java.util.ArrayList;
import java.util.List;

/**
 *	A class used to force a specific number of loops of an audio stream.
 *	The class can buffer the stream, if you consider that it will be small
 *	enough to fit within memory, otherwise it will reset the audio stream
 *	at each loop.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 27 Nov 2011
 */
public class AudioLooper extends AudioStream
{
	/** The audio stream to loop */
	private AudioStream stream = null;
	
	/** The number of times to loop */
	private int nLoops = 1;
	
	/** The current loop being played */
	private int currentLoop = 0;
	
	/** If we're caching the stream, it will be put in this list */ 
	private List<SampleChunk> streamCache = null;
	
	/** The index into the cache if we're playing from the cache */
	private int currentCacheIndex = 0;

	/** Whether we're caching the stream or not */
	private boolean cacheStream = false;
	
	/**
	 * 	Create a new looper that will loop the given stream the given number
	 * 	of times.
	 * 
	 *	@param stream The stream to loop
	 *	@param nLoops The number of times to loop the stream
	 */
	public AudioLooper( AudioStream stream, int nLoops )
	{
		this( stream, nLoops, false );
	}
	
	/**
	 * 	Create a new looper that will loop the given stream the given number
	 * 	of times. Whether the stream is cached or not is able to be specified. 
	 * 
	 *	@param stream The stream to loop
	 *	@param nLoops The number of loops to do
	 *	@param cacheStream Whether to cache the stream or not
	 */
	public AudioLooper( AudioStream stream, int nLoops, boolean cacheStream )
	{
		this.stream = stream;
		this.nLoops = nLoops;
		this.cacheStream  = cacheStream;
		this.format = stream.getFormat();
		
		if( cacheStream )
			streamCache = new ArrayList<SampleChunk>();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.AudioStream#nextSampleChunk()
	 */
	@Override
	public SampleChunk nextSampleChunk()
	{
		if( currentLoop == 0 || !cacheStream )
		{
			SampleChunk sc = stream.nextSampleChunk();
			
			if( sc != null )
			{
				// Cache the chunk if necessary
				if( cacheStream && nLoops > 1 )
					streamCache.add( sc.clone() );
				
				// We're on the first loop, so simply return this chunk
				return sc;
			}
			else	
			{
				currentCacheIndex = 0;
				currentLoop++;
				
				// If we're not caching the stream, we need to reset the
				// stream.
				if( !cacheStream )
				{
					stream.reset();
					return stream.nextSampleChunk();
				}
			}
		}
		
		if( currentLoop < nLoops )
		{
			SampleChunk sc = streamCache.get( currentCacheIndex );
			currentCacheIndex++;
			
			if( currentCacheIndex == streamCache.size() )
			{
				currentCacheIndex = 0;
				currentLoop++;
			}
		
			return sc; 
		}
		
		return null;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.AudioStream#reset()
	 */
	@Override
	public void reset()
	{
	}
}
