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
package org.openimaj.video.processing.shotdetector;

import org.openimaj.image.Image;
import org.openimaj.video.timecode.VideoTimecode;

/**
 *	Represents a shot boundary that is a fade shot boundary between
 *	two scenes. This occurs over a time duration so has both a start
 *	and end timecode.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	@param <T> Type of image 
 *  @created 7 Jun 2011
 *	
 */
public class FadeShotBoundary<T extends Image<?,T>> extends ShotBoundary<T>
{
	/** The timecode at the end of the fade */
	private VideoTimecode endTimecode;
	
	/**
	 * 	Create a new fade shot boundary giving the start
	 * 	of the fade as a starting point.
	 * 
	 *	@param b The boundary at the start of the fade.
	 */
	public FadeShotBoundary( ShotBoundary<T> b )
	{
		super( b.timecode );
	}
	
	/**
	 * 	Get the timecode at the end of the fade.
	 *	@return The timecode at the end of the fade.
	 */
	public VideoTimecode getEndTimecode()
	{
		return this.endTimecode;
	}
	
	/**
	 * 	Get the timecode at the start of the fade.
	 *	@return The timecode at the start of the fade.
	 */
	public VideoTimecode getStartTimecode()
	{
		return super.timecode;
	}
	
	/**
	 * 	Set the timecode at the end of the fade. 
	 *	@param v The timecode of end of the fade
	 */
	public void setEndTimecode( VideoTimecode v )
	{
		this.endTimecode = v;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.processing.shotdetector.ShotBoundary#getTimecode()
	 */
	@Override
	public VideoTimecode getTimecode()
	{
		return this.getEndTimecode();
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.processing.shotdetector.ShotBoundary#toString()
	 */
	@Override
	public String toString()
	{
		return "Fade "+getStartTimecode()+" -> "+getEndTimecode();
	}
}
