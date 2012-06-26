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
package org.openimaj.audio.timecode;

import org.openimaj.audio.util.MusicUtils;

/**
 *	A standard MIDI-timecode that uses measures, beats and ticks. A beat is
 *	determined by the BPM of the track, while ticks is a given subdivision
 *	of a beat. Measures is based on the meter of the track.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 28 Nov 2011
 */
public class MeasuresBeatsTicksTimecode extends AudioTimecode
{
	/** Most western music is in four-time */
	public int beatsPerMeasure = 4;
	
	/** Standard MIDI is generally 120 ticks per beat */
	public int ticksPerBeat = 120;
	
	/** Number of beats per minute */
	public float bpm = 140;

	/** At the given bpm, the number of milliseconds per beat */
	private int mpb = MusicUtils.millisPerBeat( bpm );
	
	/**
	 * 	Sets up a 4-time, 120 tick MBT timecode.
	 *	@param bpm
	 */
	public MeasuresBeatsTicksTimecode( float bpm )
	{
		super( 0 );
		this.bpm = bpm;
		this.mpb = MusicUtils.millisPerBeat( bpm );
	}
	
	/**
	 * 	Create a specific timecode.
	 * 
	 *	@param bpm The number of beats per minute
	 *	@param measures The number of measures
	 *	@param beats The number of beats
	 *	@param ticks The number of ticks
	 */
	public MeasuresBeatsTicksTimecode( float bpm, long measures, int beats, 
			int ticks )
	{
		this( bpm );
		int mpt = mpb / ticksPerBeat;
		super.milliseconds = mpb * ((measures*beatsPerMeasure)+beats) + 
			(mpt*ticks);
	}

	/**
	 *	Create a specific timecode in a different time signature.
	 * 
	 *	@param bpm The number of beats per minute
	 *	@param measures The number of measures
	 *	@param beats The number of beats
	 *	@param ticks The number of ticks
	 *	@param beatsPerMeasure The number of beats in a measure
	 */
	public MeasuresBeatsTicksTimecode( float bpm, long measures, int beats, 
			int ticks, int beatsPerMeasure )
	{
		this( bpm );
		this.beatsPerMeasure = beatsPerMeasure;
		int mpt = mpb / ticksPerBeat;
		super.milliseconds = mpb * ((measures*beatsPerMeasure)+beats) + 
			(mpt*ticks);
	}

	/**
	 * 	Returns the number of measures.
	 *	@return The number of measures.
	 */
	public long getMeasures()
	{
		return (milliseconds / mpb) / beatsPerMeasure;
	}
	
	/**
	 * 	Returns the number of beats.
	 *	@return The number of beats.
	 */
	public int getBeats()
	{
		return (int)(milliseconds / mpb) % beatsPerMeasure;
	}
	
	/**
	 *	Returns the number of ticks in the current beat. 
	 *	@return The number of ticks in the current beat.
	 */
	public int getTicks()
	{
		float mpt = mpb / (float)ticksPerBeat;
		return (int)(milliseconds / mpt) % ticksPerBeat;
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.timecode.AudioTimecode#toString()
	 */
	@Override
	public String toString()
	{
		return getMeasures()+":"+getBeats()+":"+getTicks();
	}
}
