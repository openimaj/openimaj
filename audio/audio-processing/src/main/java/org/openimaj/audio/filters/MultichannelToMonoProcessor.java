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

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.AudioProcessor;

/**
 *	Converts a stereo audio stream into a mono one by averaging the
 *	channels' samples and creating a mono sample set. The audio
 *	format of the file is changed, so the properties of the stream are updated
 *	when processing is complete. This means that if you call
 *	{@link #process(SampleChunk)} yourself you must manually update the
 *	audio format details as they are only updated during a complete process.
 *	The sample-rate is unchanged.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 10 Jun 2011
 *	
 */
public class MultichannelToMonoProcessor extends AudioProcessor
{
	/**
	 * 	Create a processor to process chunks.
	 */
	public MultichannelToMonoProcessor()
	{
	}
	
	/**
	 *	Create a processor for the given audio stream. The output
	 *	of this audio stream will be a mono stream.
	 * 
	 *	@param a The audio stream to process.
	 */
	public MultichannelToMonoProcessor( AudioStream a )
	{
		super( a );
		getFormat().setNumChannels( 1 );
	}
	
	/** 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process( SampleChunk sample )
	{
		if( sample.getFormat().getNumChannels() == 1 )
			return sample;
		
		// Get the samples.
		ShortBuffer sb = sample.getSamplesAsByteBuffer().asShortBuffer();
		int nChannels = sample.getFormat().getNumChannels();
		
		// Create a new buffer for the mono samples.
		byte[] monoBuffer = new byte[ sb.limit() / nChannels * Short.SIZE/Byte.SIZE ];
		ShortBuffer sb2 = ByteBuffer.wrap( monoBuffer ).
			order( sb.order() ).asShortBuffer();
		
		// For all the mono samples...
		for( int i = 0; i < sb2.limit(); i++ )
		{
			// Accumulate the sample value 
			int acc = 0;
			for( int c = 0; c < nChannels; c++ )
				acc += sb.get(i*nChannels+c);
			
			// Store the average to the mono channel
			sb2.put( i, (short)(acc / nChannels) );
		}
			
		// Update the samples in the sample chunk 
		sample.setSamples( monoBuffer );
		return sample;
	}

	/**
	 * 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.processor.AudioProcessor#processingComplete(org.openimaj.audio.AudioStream)
	 */
	@Override
	public void processingComplete( AudioStream a )
	{
		// It's a mono file now.
		getFormat().setNumChannels( 1 );
	}
}
