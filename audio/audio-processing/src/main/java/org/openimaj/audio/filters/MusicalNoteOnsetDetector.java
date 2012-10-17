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
