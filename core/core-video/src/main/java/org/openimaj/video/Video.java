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

import java.io.Closeable;
import java.util.Iterator;

import org.openimaj.image.Image;

/**
 * Abstract base class for videos.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            the image type of the frames
 */
public abstract class Video<T extends Image<?, T>> implements Iterable<T>, Closeable
{
	/** The number of frames per second */
	// protected double fps = -1;

	/** The current frame being displayed */
	protected int currentFrame;

	/**
	 * Get the next frame. Increments the frame counter by 1.
	 * 
	 * @return the next frame
	 */
	public abstract T getNextFrame();

	/**
	 * Get the current frame
	 * 
	 * @return the current frame
	 */
	public abstract T getCurrentFrame();

	/**
	 * Get the width of the video frame
	 * 
	 * @return the width of the video frame.
	 */
	public abstract int getWidth();

	/**
	 * Get the height of the video frame.
	 * 
	 * @return the height of the video frame.
	 */
	public abstract int getHeight();

	/**
	 * Get the timestamp of the current frame in milliseconds
	 * 
	 * @return the time stamp in milliseconds
	 */
	public abstract long getTimeStamp();

	// /**
	// * Determine how many milliseconds each frame needs
	// * to be displayed for
	// *
	// * @return the time to show each frame in ms
	// */
	// public long getMilliPerFrame()
	// {
	// if(this.getFPS() < 0) return 1;
	// return (long) (1000 * (1.0/this.getFPS()));
	// }
	//
	/**
	 * Get the frame rate
	 * 
	 * @return the frame rate
	 */
	public abstract double getFPS();

	// /**
	// * Set the frame rate
	// *
	// * @return the frame rate
	// */
	// public void setFPS(double fps)
	// {
	// this.fps = fps;
	// }

	/**
	 * Get the index of the current frame
	 * 
	 * @return the current frame index
	 */
	public synchronized int getCurrentFrameIndex()
	{
		return currentFrame;
	}

	/**
	 * Set the current frame index (i.e. skips to a certain frame). If your
	 * video subclass can implement this in a cleverer way, then override this
	 * method, otherwise this method will simply grab frames until it gets to
	 * the given frame index. This method is naive and may take some time as
	 * each frame will be decoded by the video decoder.
	 * 
	 * @param newFrame
	 *            the new index
	 */
	public synchronized void setCurrentFrameIndex(long newFrame)
	{
		// We're already at the frame?
		if (this.currentFrame == newFrame)
			return;

		// If we're ahread of where we want to be
		if (this.currentFrame > newFrame)
		{
			this.reset();
		}

		// Grab frames until we read the new frame counter
		// (or until the getNextFrame() method returns null)
		while (this.currentFrame < newFrame && getNextFrame() != null)
			;
	}

	/**
	 * Seek the video to a given timestamp in SECONDS. Many videos (including
	 * cameras etc.) will have no ability to seek so by default this function
	 * does nothing.
	 * 
	 * @param timestamp
	 */
	public void seek(double timestamp) {

	}

	/**
	 * Returns whether this video has another frame to provide.
	 * 
	 * @return Whether the video has another frame available
	 */
	public abstract boolean hasNextFrame();

	/**
	 * Return the number of frames in the whole video. If the video is a live
	 * stream, then this method should return -1.
	 * 
	 * @return the number of frames in the whole video or -1 if unknown
	 */
	public abstract long countFrames();

	/**
	 * Reset the video - putting the frame counter back to the start.
	 */
	public abstract void reset();

	@Override
	public Iterator<T> iterator() {
		return new VideoIterator<T>(this);
	}

	/**
	 * Close the video object
	 */
	@Override
	public void close() {

	}
}
