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
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
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
