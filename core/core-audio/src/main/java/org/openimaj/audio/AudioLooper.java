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

import java.util.ArrayList;
import java.util.List;

/**
 *	A class used to force a specific number of loops of an audio stream.
 *	The class can buffer the stream, if you consider that it will be small
 *	enough to fit within memory, otherwise it will reset the audio stream
 *	at each loop.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
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
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.AudioStream#getLength()
	 */
	@Override
	public long getLength()
	{
		return this.stream.getLength() * nLoops;
	}
}
