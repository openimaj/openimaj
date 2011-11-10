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

/**
 *	Test for the volume adjustment audio processor
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created Nov 10, 2011
 *	@version $Author$, $Revision$, $Date$
 */
public class VolumeAdjustProcessorTest
{
    /**
     *  Synthesize a 16-bit sample
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
            else    bo = ByteOrder.LITTLE_ENDIAN;

            ShortBuffer sb = ByteBuffer.wrap( b ).order( bo ).asShortBuffer();

            for( int i = 0; i < l; i++ )
                    sb.put( i, (short)i );

            return b;
    }

    /**
     * 	Synthesises a random sample, then runs it through the volume adjust
     * 	processor to ensure the correct output.
     */
    @Test
    public void testVolumeAdjust()
    {
        int sampleSize = 100;

        // Create a new sample chunk
        AudioFormat af = new AudioFormat( 16, 44.1, 1 );
        SampleChunk s = new SampleChunk( af );

        // Synthesise some noise to put in the sample
        byte[] b = synthesize( sampleSize, s );
        s.setSamples( b );
		ShortBuffer origSamples = s.getSamplesAsByteBuffer().asShortBuffer();

        System.out.println( "Generated: "+Arrays.toString(b) );
    	
        double adjustment = 0.5d;
        
        try
		{
			// Create the buffer of expected results. It will be 2* the size of the
			// samples (16 bit) and each value will be multiplied by the adjustment
			// factor above.
			byte[] expectedBytes = new byte[sampleSize*2];
			ShortBuffer sb = ByteBuffer.wrap( expectedBytes ).order(
					af.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN )
						.asShortBuffer();
			for( int j = 0; j < sampleSize; j++ )
				sb.put( j, (short)(origSamples.get(j)*adjustment) );
			
			// Now run the volumne adjust processor
			VolumeAdjustProcessor vap = new VolumeAdjustProcessor( adjustment );
			SampleChunk s2 = vap.process( s );

			System.out.println( "Expected : "+Arrays.toString( expectedBytes ) );
			System.out.println( "Got      : "+Arrays.toString( s2.getSamples() ) );
			
			// Compare the samples
			// Assert the sample chunk's buffer is the same as the expected buffer.
			Assert.assertArrayEquals( expectedBytes, s2.getSamples() );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
    }
}
