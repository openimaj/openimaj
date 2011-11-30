/**
 * 
 */
package org.openimaj.audio.samples;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.SampleChunk;

/**
 * 	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@created 23rd November 2011
 */
public abstract class SampleBufferFactory 
{
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
