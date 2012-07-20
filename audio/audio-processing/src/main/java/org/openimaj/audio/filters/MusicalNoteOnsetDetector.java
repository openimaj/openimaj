/**
 * 
 */
package org.openimaj.audio.filters;

import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.AudioProcessor;

/**
 *	
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 22 May 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class MusicalNoteOnsetDetector extends AudioProcessor
{
	/** The filter bank to use */
	private MusicalNoteFilterBank filterBank = new MusicalNoteFilterBank( 60, 72, null );
	
	/** The previous calculated output power */
	private double previousPower = 0;
	
	/** Whether an onset was detected during the last process */
	private boolean onsetDetected = false;
	
	/** The threshold over which onset is detected */
	private double threshold = 100;
	
	/** 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process( SampleChunk sample ) throws Exception
	{
		filterBank.process( sample );
		double p = filterBank.getOutputPower();
		
		double delta = Math.abs( this.previousPower - p );
		
		if( delta > threshold )
			this.onsetDetected = true;
		
		this.previousPower = p;
		
		return sample;
	}

	/**
	 * 	Returns whether an onset was detected during the last processing cycle.
	 *	@return TRUE if an onset was detected during the last processing cycle;
	 *		FALSE otherwise.
	 */
	public boolean hasOnsetBeenDetected()
	{
		return this.onsetDetected;
	}
}
