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

import org.openimaj.time.Timecode;

/**
 * 	A basic audio timecode that represents the number of milliseconds from
 * 	the start of the audio file.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 21 Nov 2011
 */
public class AudioTimecode implements Timecode
{
	/** The number of milliseconds from the start of the audio file */
	protected long milliseconds = 0;
	
	/**
	 * 	Default constructor that takes the number of milliseconds
	 * 	into the audio file.
	 * 
	 *  @param milliseconds The number of milliseconds from the start of the
	 *  	audio file.
	 */
	public AudioTimecode( long milliseconds )
    {
		this.milliseconds = milliseconds;
    }
	
	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.time.Timecode#getTimecodeInMilliseconds()
	 */
	@Override
	public long getTimecodeInMilliseconds()
	{
		return milliseconds;	
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.time.Timecode#setTimecodeInMilliseconds(long)
	 */
	@Override
	public void setTimecodeInMilliseconds( long timeInMilliseconds )
	{
		this.milliseconds = timeInMilliseconds;
	}
	
	/**
	 *  {@inheritDoc}
	 *  @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
	    return "AudioTimecode: "+this.milliseconds;
	}
	
	/**
	 *  {@inheritDoc}
	 */
	@Override
	public AudioTimecode clone()
	{
		return new AudioTimecode( milliseconds );
	}
}
