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
package org.openimaj.audio.util;

import org.openimaj.audio.timecode.MeasuresBeatsTicksTimecode;
import org.openimaj.time.TimeKeeper;

/**
 *	A timekeeper that generates {@link MeasuresBeatsTicksTimecode}s that allow
 *	the position within a music score to be tracked.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 12 Feb 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class BasicMusicTimekeeper implements TimeKeeper<MeasuresBeatsTicksTimecode>
{
	/** The time the timekeeper is started */
	private long lastStarted;
	
	/** The time to offset if the timekeeper is paused and restarted */
	private long timeOffset;
	
	/** The time the timekeeper was paused */
	private long pausedAt;

	/** Whether the timekeeper is running or not */
	private boolean isRunning;

	/** The last calculated timestamp - with offsets */
	private long currentTime;

	/** The timecode */
	private MeasuresBeatsTicksTimecode timecode = 
			new MeasuresBeatsTicksTimecode(	120, 0, 0, 0, 4 );
	
	/**
	 * 	Set the tempo of the timekeeper
	 *	@param bpm The new beats per minute.
	 */
	public void setBPM( final float bpm )
	{
		this.timecode = new MeasuresBeatsTicksTimecode(	bpm, 
				this.timecode.getMeasures(), this.timecode.getBeats(), 
				this.timecode.getTicks(), this.timecode.beatsPerMeasure );
	}
	
	/**
	 * 	Returns the number of BPMs this timekeeper is running at.
	 *	@return The BPMs
	 */
	public double getBPM()
	{
		return this.timecode.bpm;
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.time.TimeKeeper#run()
	 */
	@Override
	public void run()
	{
		if( this.lastStarted == 0 )
			this.lastStarted = System.currentTimeMillis();
		else
			if( this.supportsPause() )
				this.timeOffset += System.currentTimeMillis() - this.pausedAt;
		
		this.isRunning = true;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.time.TimeKeeper#pause()
	 */
	@Override
	public void pause()
	{
		if( this.supportsPause() )
		{
			this.isRunning = false;
			this.pausedAt = System.currentTimeMillis();
		}
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.time.TimeKeeper#stop()
	 */
	@Override
	public void stop()
	{
		this.isRunning = false;
		this.currentTime = 0;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.time.TimeKeeper#getTime()
	 */
	@Override
	public MeasuresBeatsTicksTimecode getTime()
	{
		if( this.isRunning )
		{
			// Update the current time.
			this.currentTime = (System.currentTimeMillis() -
					this.lastStarted - this.timeOffset);
			this.timecode.setTimecodeInMilliseconds( this.currentTime );
		}

		return this.timecode;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.time.TimeKeeper#seek(long)
	 */
	@Override
	public void seek( final long timestamp )
	{
		// Doesn't support seek
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.time.TimeKeeper#reset()
	 */
	@Override
	public void reset()
	{
		this.lastStarted = 0;
		this.pausedAt = -1;
		this.run();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.time.TimeKeeper#supportsPause()
	 */
	@Override
	public boolean supportsPause()
	{
		return true;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.time.TimeKeeper#supportsSeek()
	 */
	@Override
	public boolean supportsSeek()
	{
		return false;
	}

	/**
	 * 	Returns the number of ticks per beat
	 *	@return the number of ticks per beat
	 */
	public int getTicksPerBeat()
	{
		return this.timecode.ticksPerBeat;
	}
}