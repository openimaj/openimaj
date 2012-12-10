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
 *	A processor that processes the audio file by adjusting the volume
 *	by a given factor.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 8 Jun 2011
 *	
 */
public class VolumeAdjustProcessor extends AudioProcessor
{
	/** The factor to adjust the volume by */
	private double factor = 1;
	
	/**
	 * 	Default constructor that takes the volume adjustment
	 * 	factor as a double.
	 *  @param factor 
	 */
	public VolumeAdjustProcessor( final double factor )
	{
		this( factor, null );
	}
	
	/**
	 * 	Constructor that takes the volume adjustment factor to apply
	 * 	to the given stream. This allows this processor to be chainable.
	 * 
	 *	@param factor the factor to apply
	 *	@param a The audio stream to apply the factor to
	 */
	public VolumeAdjustProcessor( final double factor, final AudioStream a )
	{
		super( a );
		this.factor = factor;
	}
	
	/**
	 *	@throws Exception 
	 * {@inheritDoc}
	 * 	@see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process( final SampleChunk sample ) throws Exception
	{		
		switch( sample.getFormat().getNBits() )
		{
			case 16:
			{
				final ShortBuffer b = sample.getSamplesAsByteBuffer().asShortBuffer();
				for( int x = 0; x < b.limit(); x++ )
					b.put( x, (short)(b.get( x )*this.factor) );
				break;
			}
			case 8:
			{
				final ByteBuffer b = sample.getSamplesAsByteBuffer();
				for( int x = 0; x < b.limit(); x++ )
					b.put( x, (byte)(b.get( x )*this.factor) );
				break;
			}
			default:
				throw new Exception( "Unsupported Format" );
		}
		
		return sample;
	}
}
