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
package org.openimaj.video.timecode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 	A timecode representation that extends the standard frame count representation
 * 	to provide hours, minutes, seconds and frames timecode.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 6 Jun 2011
 */
public class HrsMinSecFrameTimecode extends FrameNumberVideoTimecode
{
	/**
	 * 	Default constructor that takes the current (start) frame number
	 * 	and the number of frames in one second.
	 * 
	 *  @param number The frame number.
	 *  @param framesPerSec The number of frames per second.
	 */
	public HrsMinSecFrameTimecode( final long number, final double framesPerSec )
	{
		super( number, framesPerSec );
	}

	/**
	 * 	Get the number of hours.
	 *  @return The number of hours.
	 */
	public int getHours()
	{
		return (int)(this.getFrameNumber() / this.fps / 3600d);		
	}
	
	/**
	 * 	Get the number of minutes within the hour.
	 *  @return The number of minutes within the hour.
	 */
	public int getMinutes()
	{
		return (int)(this.getFrameNumber() / this.fps / 60d) % 60;
	}
	
	/**
	 * 	Get the number of seconds within the minute.
	 *  @return The number of seconds within the minute.
	 */
	public int getSeconds()
	{
		return (int)(this.getFrameNumber() / this.fps) % 60;
	}
	
	/**
	 * 	Get the number of frames within the second.
	 *  @return The number of frames within the second.
	 */
	public int getFrames()
	{
		return (int)(this.getFrameNumber() % this.fps);
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.video.timecode.FrameNumberVideoTimecode#toString()
	 */
	@Override
	public String toString()
	{
	    return this.getHours()+":"+this.getMinutes()+":"+this.getSeconds()+":"
	    		+this.getFrames()+"/"+this.fps;
	}

	/**
	 * 	Parses a string (formatted as {@link #toString()}) back into a
	 * 	timecode object.
	 * 
	 *	@param s The string to parse
	 *	@return A new {@link HrsMinSecFrameTimecode} or null if the string
	 *		couldn't be parsed.
	 */
	public static HrsMinSecFrameTimecode fromString( final String s )
	{
		final Pattern p = Pattern.compile( "(\\d+):(\\d+):(\\d+):(\\d+)/([\\d.]+)");
		final Matcher m = p.matcher( s );

		// Check for a match
		if( !m.find() )
			return null;
		
		// Extract all the bits and bobs
		final double fps = Double.parseDouble(m.group(3));
		final int hrs = Integer.parseInt( m.group(0) );
		final int min = Integer.parseInt( m.group(1) );
		final int sec = Integer.parseInt( m.group(2) );
		final int frames = Integer.parseInt( m.group(3) );
		
		// Work out the frame number from the bits and bobs
		final int frameNumber = (int)(frames + sec*fps + min*60*fps + hrs*3600*fps);
		
		// Create a new object
		final HrsMinSecFrameTimecode h = new HrsMinSecFrameTimecode( frameNumber, fps );
		return h;
	}
	
	/**
	 *  {@inheritDoc}
	 *  @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
	    return this.toString().hashCode();
	}

	/**
	 *  {@inheritDoc}
	 *  @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( final Object obj )
	{
		if( obj instanceof HrsMinSecFrameTimecode )
		{
			final HrsMinSecFrameTimecode h = (HrsMinSecFrameTimecode)obj;
			return h.getHours() == this.getHours() &&
				h.getMinutes() == this.getMinutes() &&
				h.getSeconds() == this.getSeconds() &&
				h.getFrames() == this.getFrames();
		}
		
		return false;
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.timecode.FrameNumberVideoTimecode#clone()
	 */
	@Override
	public HrsMinSecFrameTimecode clone()
	{
		return new HrsMinSecFrameTimecode( this.getFrameNumber(), this.fps );
	}
}
