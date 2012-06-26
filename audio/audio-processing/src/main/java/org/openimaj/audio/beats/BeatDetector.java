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
package org.openimaj.audio.beats;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.AudioProcessor;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.audio.timecode.AudioTimecode;

/**
 * 	A beat detector that uses a 2nd order LP filter, followed by an envelope 
 * 	detector (thanks Bram), feeding a Schmitt trigger. The rising edge detector 
 * 	provides a 1-sample pulse each time a beat is detected. The class also 
 * 	provides beat detection per sample chunk.
 * 
 * 	@see "http://www.musicdsp.org/showArchiveComment.php?ArchiveID=200"
 * 	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	
 * 	@created 30 Nov 2011
 */
public class BeatDetector extends AudioProcessor
{
	/** Filter coefficient */
	private float kBeatFilter;

	private float filter1Out, filter2Out;

	/** Release time coefficient */
	private float beatRelease;

	/** Peak envelope follower */
	private float peakEnv;

	/** Schmitt trigger output */
	private boolean beatTrigger;

	/** Rising edge memory */
	private boolean prevBeatPulse;

	/** Beat detector output */
	private boolean beatPulse;

	/** The timecode of the detected beat */
	private AudioTimecode beatTimecode = new AudioTimecode(0);
	
	/** Whether a beat has been detected within a sample chunk */
	private boolean beatDetected = false;

	/** Low Pass filter frequency */
	public static final float FREQ_LP_BEAT = 150.0f;

	/** Low Pass filter time constant */
	public static final float T_FILTER = (float) (1.0f / (2.0f * Math.PI * FREQ_LP_BEAT));

	/** Release time of envelope detector in seconds */
	public static final float BEAT_RTIME = 0.02f;
	
	/**
	 * 	Default constructor
	 * 	@param af The format of the incoming data.
	 */
	public BeatDetector( AudioFormat af )
	{
		filter1Out = 0.0f;
		filter2Out = 0.0f;
		peakEnv = 0.0f;
		beatTrigger = false;
		prevBeatPulse = false;
		this.format = af;
		this.setSampleRate( (float)(af.getSampleRateKHz()*1000f) );
	}

	/**
	 * 	Set the sample rate of the incoming data.
	 *	@param sampleRate The sample rate
	 */
	private void setSampleRate( float sampleRate )
	{
		kBeatFilter = (float) (1.0 / (sampleRate * T_FILTER));
		beatRelease = (float) Math.exp( -1.0f / (sampleRate * BEAT_RTIME) );
	}

	/**
	 * 	{@inheritDoc}
	 * 	@see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process( SampleChunk samples )
	{
		// Detect beats. Note that we stop as soon as we detect a beat.
		this.beatDetected = false;
		SampleBuffer sb = samples.getSampleBuffer();
		int i = 0;
		for(; i < sb.size(); i++ )
		{
			if( this.beatDetected = processSample( sb.get(i) ) )
				break;
		}
		
		if( this.beatDetected() )
			beatTimecode.setTimecodeInMilliseconds( (long)( 
				samples.getStartTimecode().getTimecodeInMilliseconds() +
				i * this.format.getSampleRateKHz() ) );
		
//		System.out.println( beatDetected );
		
		// We return the samples unaltered
		return samples;
	}
	
	/**
	 * 	Process a given sample.
	 *	@param input The sample to process.
	 *	@return TRUE if a beat was detected at this sample
	 */
	private boolean processSample( float in )
	{
		float EnvIn;
		
		float input = in / Integer.MAX_VALUE;

		// Step 1 : 2nd order low pass filter (made of two 1st order RC filter)
		filter1Out = filter1Out + (kBeatFilter * (input - filter1Out));
		filter2Out = filter2Out + (kBeatFilter * (filter1Out - filter2Out));

		// Step 2 : peak detector
		EnvIn = Math.abs( filter2Out );
		if( EnvIn > peakEnv )
			peakEnv = EnvIn; // Attack time = 0
		else
		{
			peakEnv *= beatRelease;
			peakEnv += (1.0f - beatRelease) * EnvIn;
		}

		// Step 3 : Schmitt trigger
		if( !beatTrigger )
		{
			if( peakEnv > 0.3 ) beatTrigger = true;
		}
		else	
		{
			if( peakEnv < 0.15 ) beatTrigger = false;
		}

		// Step 4 : rising edge detector
		beatPulse = false;
		if( (beatTrigger) && (!prevBeatPulse) ) beatPulse = true;
		prevBeatPulse = beatTrigger;
		
		return beatPulse;
	}
	
	/**
	 * 	Returns whether a beat was detected within this sample chunk.
	 *	@return TRUE if a beat was detected
	 */
	public boolean beatDetected()
	{
		return this.beatDetected;
	}
	
	/**
	 * 	Returns the timecode at which the first beat in this sample chunk
	 * 	was detected. Note that this class reuses this timecode class, so if
	 * 	you wish to use it afterwards you should clone it immediately after
	 * 	calling this function.
	 * 
	 *	@return The beat timecode.
	 */
	public AudioTimecode getBeatTimecode()
	{
		return this.beatTimecode;
	}
}
