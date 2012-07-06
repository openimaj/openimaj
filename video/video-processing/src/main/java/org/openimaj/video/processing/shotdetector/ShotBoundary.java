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
 * 	A class for encapsulating shot boundary information.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	@param <T> The type of image 
 *	
 *	@created 1 Jun 2011
 */
public class ShotBoundary<T extends Image<?,T>>
{	
	/** The timecode of the shot boundary */
	protected VideoTimecode timecode = null;
	
	/** The keyframe of the boundary. May not be set */
	protected VideoKeyframe<T> keyframe = null;

	/**
	 * 	Construct a shot boundary using the given video timecode.
	 * 
	 *  @param timecode The timecode
	 */
	public ShotBoundary( VideoTimecode timecode )
	{
		this.timecode = timecode;
	}

	/**
	 * 	Get the timecode of this shot boundary.
	 *	@return The timecode of this shot boundary;
	 */
	public VideoTimecode getTimecode()
	{
		return this.timecode;
	}
	
	/**
	 *  {@inheritDoc}
	 *  @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
	    return this.timecode.toString();
	}

	/**
	 *	@return the keyframe
	 */
	public VideoKeyframe<T> getKeyframe()
	{
		return keyframe;
	}

	/**
	 *	@param keyframe the keyframe to set
	 */
	public void setKeyframe( VideoKeyframe<T> keyframe )
	{
		this.keyframe = keyframe;
	}
}
