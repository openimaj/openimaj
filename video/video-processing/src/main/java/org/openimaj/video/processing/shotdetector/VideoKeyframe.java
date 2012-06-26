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
 * 	A class that represents a keyframe of a video. Encapsualtes the image
 * 	representing the keyframe as well as the timecode at which the keyframe
 * 	occurs in the video.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 * 	@param <T> the {@link Image} type 
 *	@created 1 Jun 2011
 */
public class VideoKeyframe<T extends Image<?,T>>
{
	/** An image at the shot boundary */
	public T imageAtBoundary = null;
	
	/** The timecode of the keyframe */
	public VideoTimecode timecode = null;
	
	/**
	 * 	Constructor that allows construction of an image-based shot
	 * 	boundary.
	 * 
	 *  @param timecode The timecode of the shot boundary
	 *  @param img The image at the shot boundary
	 */
	public VideoKeyframe( VideoTimecode timecode, T img )
	{
	    this.imageAtBoundary = img;
	    this.timecode = timecode;
    }
	
	/**
	 * 	Return the image at the shot boundary.
	 *  @return The image at the shot boundary.
	 */
	public T getImage()
	{
		return imageAtBoundary;
	}
	
	/**
	 * 	Returns the timecode of this keyframe.
	 *	@return The timecode of the keyframe in the video.
	 */
	public VideoTimecode getTimecode()
	{
		return this.timecode;
	}
	
	/**
	 *	{@inheritDoc}
	 */
	@Override
	public VideoKeyframe<T> clone()
	{
		return new VideoKeyframe<T>( this.timecode.clone(), 
				this.imageAtBoundary.clone() );
	}
}
