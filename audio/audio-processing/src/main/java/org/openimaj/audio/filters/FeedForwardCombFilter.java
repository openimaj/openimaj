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
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 27 Apr 2012
 *	
 */
public class FeedForwardCombFilter extends FixedSizeSampleAudioProcessor
{
	/** The sample buffer that is used for dealing with the overlap sample chunks */
	private SampleBuffer buffer = null;
	
	/** The gain of the delayed signal */
	private double gain = 1d;
	
	/** The power of each frame of the output */
	private double outputPower = 0;
	
	/** The power of each frame of the input */
	private double inputPower = 0;
	
	/** The frequency being detected */
	private double frequency = 0;
	
	/**
	 * 	Constructor that takes the number of samples delay
	 * 	to apply to the signal.
	 * 
	 *	@param nSamplesDelay The number of delay samples
	 *	@param gain The gain of the delayed signal
	 */
	public FeedForwardCombFilter( final int nSamplesDelay, final double gain )
	{
		super( nSamplesDelay );
		this.gain = gain;
		this.frequency = -1;
	}
	
	/**
	 * 	Constructor that takes the frequency at which the comb filter
	 * 	will operate and the gain to apply to the delayed signal.
	 * 
	 *	@param frequency The frequency
	 *	@param sampleRate The sample rate of the signal in Hz
	 *	@param gain the gain of the delayed signal
	 */
	public FeedForwardCombFilter( final double frequency, final double sampleRate, final double gain )
	{
		this( (int)(sampleRate/frequency), gain );
		this.frequency = frequency;
	}
	
	/**
	 * 	This returns the frequency that this comb filter is set to detect.
	 * 	This is only available if the frequency was used during construction.
	 *  @return The frequency.
	 */
	public double getFrequency()
	{
		return this.frequency;
	}

	/** 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process( final SampleChunk sample ) throws Exception
	{
		// If we don't yet have a buffer, we must be at the start of
		// the stream, so we pass the first samples on unedited and store
		// them for the delayed signal.
		if( this.buffer == null )
		{
			this.buffer = sample.getSampleBuffer();
			return sample;
		}
		
		// Make a copy of the sample chunk and store it for later.
		this.buffer = sample.clone().getSampleBuffer();
		
		// We'll side-affect the incoming chunk here
		double p = 0;
		double ip = 0;
		final SampleBuffer b = sample.getSampleBuffer();
		for( int i = 0; i < b.size(); i++ )
		{
			final float d = (float)(b.get(i) - this.gain*this.buffer.get(i));
			p += d*d;
			ip += b.get(i) * b.get(i);
			b.set( i, d );
		}
		
		this.outputPower = p;
		this.inputPower = ip;
		
		// Return the incoming chunk, side-affected.
		return sample;
	}
	
	/**
	 * 	Returns the output power for the last processed frame.
	 * 	@return the output power for the last processed frame.
	 */
	public double getOutputPower()
	{
		return this.outputPower;
	}
	
	/**
	 * 	Returns the input power for the last processed frame. 
	 *  @return the input power for the last processed frame.
	 */
	public double getInputPower()
	{
		return this.inputPower;
	}
	
	/**
	 * 	Returns the harmonicity power.
	 *  @return the harmonicity power.
	 */
	public double getHarmonicity()
	{
		return this.getOutputPower() / (this.getInputPower()*4);
	}
}
