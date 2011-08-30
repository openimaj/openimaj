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
package org.openimaj.video;

import org.openimaj.image.Image;

/**
 * Abstract base class for videos.
 * 
 * @author Sina Samangooei <ss@ecs.soton.ac.uk>
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 * @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *
 * @param <T> the image type of the frames
 */
public abstract class Video<T extends Image<?,T>> 
{
	/** The number of frames per second */
	protected double fps;
	
	/** The current frame being displayed */
	protected int currentFrame;
	
	
	/**
	 * Get the next frame. Increments the frame counter by 1. 
	 * @return the next frame
	 */
	public abstract T getNextFrame();
	
	/**
	 * Get the current frame
	 * @return the current frame
	 */
	public abstract T getCurrentFrame();
	
	/**
	 * 	Get the width of the video frame
	 *  @return the width of the video frame.
	 */
	public abstract int getWidth();
	
	/**
	 * 	Get the height of the video frame.
	 *  @return the height of the video frame.
	 */
	public abstract int getHeight();
	
	/**
	 * Determine how many milliseconds each frame needs
	 * to be displayed for
	 * @return the time to show each frame in ms
	 */
	public long getMilliPerFrame()
	{
		if(this.getFPS() < 0) return 1;
		return (long) (1000 * (1.0/this.getFPS()));
	}
	
	/**
	 * Get the frame rate
	 * @return the frame rate
	 */
	public double getFPS() 
	{
		return fps;
	}
	
	/**
	 * Set the frame rate
	 * @return the frame rate
	 */
	public void setFPS(double fps) 
	{
		this.fps = fps;
	}
	
	/**
	 * Get the index of the current frame
	 * @return the current frame index
	 */
	public synchronized int getCurrentFrameIndex() 
	{
		return currentFrame;
	}
	
	/**
	 * Set the current frame index (i.e. skips to a certain frame)
	 * @param newFrame the new index
	 */
	public synchronized void setCurrentFrameIndex( int newFrame ) 
	{
		this.currentFrame = newFrame;
	}

	public abstract boolean hasNextFrame();
	
	public abstract long countFrames();

	
}
