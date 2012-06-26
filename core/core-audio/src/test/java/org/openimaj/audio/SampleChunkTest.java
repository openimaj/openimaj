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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.SampleChunk;

/**
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 1 Nov 2011
 */
public class SampleChunkTest
{
	/**
	 * 	Synthesize a 16-bit sample
	 * 
	 *  @param l Sample length
	 *  @param s The sample chunk
	 *  @return
	 */
	private byte[] synthesize( int l, SampleChunk s )
	{
		// Synthesise a 16-bit sample sample
		byte[] b = new byte[ 2 * l ];

		ByteOrder bo = null;
		
		if( s.getFormat().isBigEndian() )
				bo = ByteOrder.BIG_ENDIAN;
		else	bo = ByteOrder.LITTLE_ENDIAN;
		
		ShortBuffer sb = ByteBuffer.wrap( b ).order( bo ).asShortBuffer();
		
		for( int i = 0; i < l; i++ )
			sb.put( i, (short)i );
		
		return b;
	}
	
	/**
	 * 	Tests the {@link SampleChunk#getSampleSlice(int, int)} method
	 * 	by generating a random sample and testing that the slice is
	 * 	as expected.
	 */
	@Test
	public void testSampleSlice()
	{
		int sampleSize = 100;
		
		// Create a new sample chunk
		AudioFormat af = new AudioFormat( 16, 44.1, 1 );
		SampleChunk s = new SampleChunk( af );
		
		// Synthesise some noise to put in the sample
		byte[] b = synthesize( sampleSize, s );
		s.setSamples( b );

		System.out.println( "Generated sample: "+Arrays.toString(b) );
		
		// Do 10 tests
		for( int i = 0; i < 10; i++ )
		{
			// Take a random slice from the sample
			int start  = (int)(Math.random() * sampleSize);
			int length = (int)(Math.random() * (sampleSize-start) );
			
			// This is what we're testing
			SampleChunk sc = s.getSampleSlice( start, length );
			
			System.out.println( "   - taken slice "+start+" -> "+(start+length) );
			
			// *2 because we're 16-bit samples here
			byte[] b2 = new byte[length*2];
			for( int j = 0; j < length*2; j++ )
				b2[j] = b[j+(start*2)];
			
			System.out.println( "    - Expecting "+Arrays.toString( b2 ) );
			System.out.println( "    - Got "+Arrays.toString(sc.getSamples()));
			
			Assert.assertArrayEquals( b2, sc.getSamples() );
		}
	}
}
