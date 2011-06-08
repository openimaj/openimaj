/**
 * 
 */
package org.openimaj.audio;

import java.nio.ShortBuffer;

import org.openimaj.audio.processor.AudioProcessor;

/**
 *	A processor that processes the audio file by adjusting the volume
 *	by a given factor.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 8 Jun 2011
 *	@version $Author$, $Revision$, $Date$
 */
public class VolumeAdjustProcessor extends AudioProcessor
{
	/** The factor to adjust the volume by */
	private double factor = 1;
	
	/**
	 * 	Default constructor that takes the volume adjustment
	 * 	factor as a double.
	 */
	public VolumeAdjustProcessor( double factor )
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
	public VolumeAdjustProcessor( double factor, AudioStream a )
	{
		super( a );
		this.factor = factor;
	}
	
	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process( SampleChunk sample )
	{		
		ShortBuffer b = sample.getSamplesAsByteBuffer().asShortBuffer();
		for( int x = 0; x < b.limit(); x++ )
			b.put( x, (short)(b.get( x )*factor) );
		return sample;
	}
}
