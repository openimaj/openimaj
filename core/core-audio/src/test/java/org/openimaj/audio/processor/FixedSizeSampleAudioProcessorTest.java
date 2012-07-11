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
package org.openimaj.audio.processor;


import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;

/**
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 3 Oct 2011
 */
public class FixedSizeSampleAudioProcessorTest
{
	/**
	 * 	This test class implements the audio interface and will return
	 * 	65,536 samples of increasing values (0-127). The sample chunk size has
	 * 	been arbitrarily set to 400 bytes. The class deals with any remainders.
	 * 	The audio format will be 44.1KHz, 16 bit, mono, signed, little-endian.
	 * 	As the length is a multiple of 256 there should not be any sample
	 * 	chunks that will not be 256 in length (as returned from the
	 * 	{@link FixedSizeSampleAudioProcessor} with sample set length of 256. 
	 * 
	 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *	
	 *	@created 3 Oct 2011
	 */
	private class TestAudio extends AudioStream
	{
		private int totalLength = 65536;
		private int sampleChunkSize = 400;
		private int currentStreamPos = 0;
		
		public TestAudio()
		{
			AudioFormat f = new AudioFormat( 16, 44100, 1 );
			super.setFormat( f );
		}
				
		@Override
        public SampleChunk nextSampleChunk()
        {
			SampleChunk s = simulateSamples( sampleChunkSize );
			return s;
        }		

		private SampleChunk simulateSamples( int length )
		{
			// Create a sample chunk
			SampleChunk s = new SampleChunk( getFormat() );
			
			// This is the number of bytes we need in our sample chunk
			int l = length * getFormat().getNBits()/8 * getFormat().getNumChannels();
			
			// catch the end of the stream
			if( (currentStreamPos + l) > this.totalLength )
					l = this.totalLength - currentStreamPos;
			
			System.out.println( "currentstreampos: "+currentStreamPos+", l: "+l );
			
			// If there's nothing to add to the stream, return null
			if( currentStreamPos == this.totalLength ) return null;
			
			// Create samples
			byte[] b = new byte[ l ];
			
			for( int i = 0; i < b.length; i++ )
				b[i] = (byte)((currentStreamPos+i)%128);
			currentStreamPos += b.length;
			
			s.setSamples( b );
	        return s;
        }

		@Override
		public void reset()
		{
			currentStreamPos = 0;
		}
		
		@Override
		public long getLength()
		{
			return (long)(totalLength / format.getSampleRateKHz());
		}
	}

	/** A test audio function */
	private TestAudio audio = null;
	private int count = 0;
	
	/**
	 *  @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		audio  = new TestAudio();
	}
	
	/**
	 * 
	 */
	@Test
	public void testSampleSize()
	{
		FixedSizeSampleAudioProcessor fssap = new FixedSizeSampleAudioProcessor(256)		
		{
			@Override
			public SampleChunk process( SampleChunk sample )
			{
				System.out.println( count+" : "+Arrays.toString( sample.getSamples() ) );
				
				// Every sample set should be 256 samples...
				Assert.assertEquals( 256, sample.getNumberOfSamples() );
				
				// ...or 512 bytes...
				Assert.assertEquals( 512, sample.getSamples().length );
				
				// And the samples are being generated such that the first
				// byte should be a zero.
				Assert.assertEquals( 0, sample.getSamples()[0] );
				
				// And the last a 127
				Assert.assertEquals( 127, sample.getSamples()[255] );
				
				count++;
				
				return sample;
			}
		};
		
		try
		{
			fssap.process( audio );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		// The process function should have been called 128 times
		// (65,536 bytes, in 256 * 2 sample chunks)
		Assert.assertEquals( 65536/2/256, count );
	}
	
	/**
	 * Test overlapping windows
	 */
	@Test
	public void testOverlappingWindows()
	{
		count = 0;
		System.out.println( "-----------------------------------------------");
		final int windowStep = 16;
		FixedSizeSampleAudioProcessor fssap = new FixedSizeSampleAudioProcessor(256)		
		{
			@Override
			public SampleChunk process( SampleChunk sample )
			{
				System.out.println( count+" : "+Arrays.toString( sample.getSamples() ) );
				
				// Every sample set should be 256 samples...
				Assert.assertEquals( 256, sample.getNumberOfSamples() );
				
				// ...or 512 bytes...
				Assert.assertEquals( 512, sample.getSamples().length );
				
				// And the samples are being generated such that the first
				// byte is equal to the window step of each step. We multiply
				// window step by 2 as we're generating 16 bit values so each
				// byte of sample is double the sample number.
				Assert.assertEquals( (count*(windowStep*2))%128, 
						sample.getSamples()[0] );
				
				// And the last will be 127 away
				Assert.assertEquals( ((count*(windowStep*2))+127)%128, 
						sample.getSamples()[255] );
				
				count++;
				
				return sample;
			}
		};
		fssap.setWindowStep( windowStep );
		
		try
		{
			fssap.process( audio );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		// The process function should have been called 2032 times
		// (nsamples - window size/window step)
		Assert.assertEquals( ((65536 / 2)-256)/windowStep+1, count );	
	}
}
