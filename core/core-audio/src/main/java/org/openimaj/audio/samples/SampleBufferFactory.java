/**
 * 
 */
package org.openimaj.audio.samples;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.SampleChunk;

/**
 * Factory for creating {@link SampleBuffer}s from {@link AudioFormat}s.
 * 
 * 	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@created 23rd November 2011
 */
public abstract class SampleBufferFactory 
{
	/**
	 * Create a {@link SampleBuffer}.
	 * @param af
	 * @param size
	 * @return new {@link SampleBuffer}.
	 */
	public static SampleBuffer createSampleBuffer( AudioFormat af, int size )
	{
		switch( af.getNBits() )
		{
			case 8:
				return new SampleBuffer8Bit( af, size );
			case 16:
				return new SampleBuffer16Bit( af, size );
			default:
				return null;
		}
	}
	
	/**
	 * Create a {@link SampleBuffer}.
	 * @param s 
	 * @param af
	 * @return new {@link SampleBuffer}.
	 */
	public static SampleBuffer createSampleBuffer( SampleChunk s, AudioFormat af )
	{
		switch( af.getNBits() )
		{
			case 8:
				return new SampleBuffer8Bit( s, af );
			case 16:
				return new SampleBuffer16Bit( s, af );
			default:
				return null;
		}		
	}
}
