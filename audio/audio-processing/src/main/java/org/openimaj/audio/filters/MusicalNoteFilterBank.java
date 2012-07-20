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
	public MusicalNoteFilterBank( AudioFormat af )
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
	public MusicalNoteFilterBank( int startOfRange, int endOfRange, AudioFormat af )
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
		filters = new ArrayList<FeedForwardCombFilter>();
		for( int i = startOfRange; i < endOfRange; i++ )
		{
			// Get the frequency of the given note
			double f = WesternScaleNote.createNote( i ).frequency;
			
			// Add a feed-forward comb filter for that frequency
			filters.add( new FeedForwardCombFilter( f, 
					format.getSampleRateKHz()*1000d, 1f ) );
		}
    }

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process( SampleChunk sample ) throws Exception
	{
		if( filters == null )
		{
			this.format = sample.getFormat();
			this.setupFilters();
		}
		
		outputPower = 0;
		for( FeedForwardCombFilter filter : filters )
		{
			// Process the sample with each filter
			filter.process( sample );
			outputPower += filter.getOutputPower();
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
