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

import java.util.ArrayList;
import java.util.List;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.AudioProcessor;
import org.openimaj.audio.util.WesternScaleNote;

/**
 * 	A filter bank for musical note detection. Uses a set of comb filters
 * 	tuned to the notes that are to be detected.
 *
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 30 Apr 2012
 */
public class MusicalNoteFilterBank extends AudioProcessor
{
	/** The start of the range as a MIDI note number */
	private int startOfRange = 0;
	
	/** The end of the range as a MIDI note number */
	private int endOfRange = 0;
	
	/** The filters we'll use */
	private List<FeedForwardCombFilter> filters = null;
	
	/** The audio format that will be processed */
	private AudioFormat format = null;
	
	/** The last calculated output power of the filter bank */
	private double outputPower = 0;
	
	/**
	 * 	Create a 12-deep filter bank for detecting notes
	 * 	between C4 and C5. The audio format may be null
	 * 	if not known in advance; in this case, the filter
	 * 	bank will be setup lazily during the processing loop. 
	 * 
	 * 	@param af The audio format of the filter bank
	 */
	public MusicalNoteFilterBank( final AudioFormat af )
    {
		this( 60, 72, af );
    }
	
	/**
	 * 	Create a filter bank for detecting notes between the start
	 * 	note given and the end note given. The audio format may be null
	 * 	if not known in advance; in this case, the filter
	 * 	bank will be setup lazily during the processing loop.
	 * 
	 *  @param startOfRange The start MIDI note number to detect
	 *  @param endOfRange The end MIDI note number to detect.
	 *  @param af The format
	 */
	public MusicalNoteFilterBank( final int startOfRange, final int endOfRange, final AudioFormat af )
    {
		this.startOfRange = startOfRange;
		this.endOfRange = endOfRange;
		this.format = af;
		
		if( this.format != null )
			this.setupFilters();
    }
	
	/**
	 * 	Setup the filters based on the settings of this class.
	 */
	private void setupFilters()
	{
		this.filters = new ArrayList<FeedForwardCombFilter>();
		for( int i = this.startOfRange; i < this.endOfRange; i++ )
		{
			// Get the frequency of the given note
			final double f = WesternScaleNote.createNote( i ).frequency;
			
			// Add a feed-forward comb filter for that frequency
			this.filters.add( new FeedForwardCombFilter( f, 
					this.format.getSampleRateKHz()*1000d, 1f ) );
		}
    }

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process( final SampleChunk sample ) throws Exception
	{
		if( this.filters == null )
		{
			this.format = sample.getFormat();
			this.setupFilters();
		}
		
		this.outputPower = 0;
		for( final FeedForwardCombFilter filter : this.filters )
		{
			// Process the sample with each filter
			filter.process( sample );
			this.outputPower += filter.getOutputPower();
		}
		
		return sample;
	}
	
	/**
	 * 	Get the last calculated output power.
	 *	@return The last calcualted output power
	 */
	public double getOutputPower()
	{
		return this.outputPower;
	}
}
