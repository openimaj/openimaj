/**
 * 
 */
package org.openimaj.audio.filters;

import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.FixedSizeSampleAudioProcessor;
import org.openimaj.audio.samples.SampleBuffer;

/**
 *	A basic feed-forward comb filter.  A buffer is used to buffer the samples during the
 *	overlap of the sample chunks and it is initialised lazily, so the first
 *	time through the filter will take slightly longer. This filter extends the
 *	{@link FixedSizeSampleAudioProcessor} to ensure that the sample chunks are
 *	the same length as the required delay; this means that the buffer need only
 *	be copied in between each iteration. 
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 27 Apr 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class FeedForwardCombFilter extends FixedSizeSampleAudioProcessor
{
	/** The sample buffer that is used for dealing with the overlap sample chunks */
	private SampleBuffer buffer = null;
	
	/** The gain of the delayed signal */
	private double gain = 1d;
	
	/**
	 * 	Constructor that takes the number of samples delay
	 * 	to apply to the signal.
	 * 
	 *	@param nSamplesDelay The number of delay samples
	 *	@param gain The gain of the delayed signal
	 */
	public FeedForwardCombFilter( int nSamplesDelay, double gain )
	{
		super( nSamplesDelay );
		this.gain = gain;
	}
	
	/**
	 * 	Constructor that takes the frequency at which the comb filter
	 * 	will operate and the gain to apply to the delayed signal.
	 * 
	 *	@param frequency The frequency
	 *	@param sampleRate The sample rate of the signal
	 *	@param gain the gain of the delayed signal
	 */
	public FeedForwardCombFilter( double frequency, double sampleRate, double gain )
	{
		this( (int)(sampleRate/frequency), gain );
	}

	/** 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process( SampleChunk sample ) throws Exception
	{
		// If we don't yet have a buffer, we must be at the start of
		// the stream, so we pass the first samples on unedited and store
		// them for the delayed signal.
		if( buffer == null )
		{
			buffer = sample.getSampleBuffer();
			return sample;
		}
		
		// Make a copy of the sample chunk and store it for later.
		buffer = sample.clone().getSampleBuffer();
		
		// We'll side-affect the incoming chunk here
		SampleBuffer b = sample.getSampleBuffer();
		for( int i = 0; i < b.size(); i++ )
			b.set( i, (float)(b.get(i) - gain*buffer.get(i)) );
		
		// Return the incoming chunk, side-affected.
		return sample;
	}
}
