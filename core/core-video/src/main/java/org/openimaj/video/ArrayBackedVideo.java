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
 * A video from an array of frames
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            the image type of the frames
 */
public class ArrayBackedVideo<T extends Image<?, T>> extends Video<T>
{
	private T[] frames;
	private boolean loop;
	private double fps = 30d;

	/**
	 * Default constructor for creating array backed videos with no frames for
	 * subclasses.
	 */
	protected ArrayBackedVideo()
	{
	}

	/**
	 * Construct a video from the provided frames. Assumes a rate of 30 FPS.
	 * 
	 * @param frames
	 *            the frames
	 */
	public ArrayBackedVideo(T[] frames) {
		this.frames = frames;
		this.currentFrame = 0;
		this.fps = 30;
		this.loop = true;
	}

	/**
	 * Construct a video from the provided frames.
	 * 
	 * @param frames
	 *            the frames
	 * @param fps
	 *            the frame rate
	 */
	public ArrayBackedVideo(T[] frames, double fps) {
		this.frames = frames;
		this.currentFrame = 0;
		this.fps = fps;
		this.loop = true;
	}

	/**
	 * Construct a video from the provided frames.
	 * 
	 * @param frames
	 *            the frames
	 * @param fps
	 *            the frame rate
	 * @param loop
	 *            loop the video
	 */
	public ArrayBackedVideo(T[] frames, double fps, boolean loop) {
		this.frames = frames;
		this.currentFrame = 0;
		this.fps = fps;
		this.loop = loop;
	}

	@Override
	public synchronized T getNextFrame() {
		final T frame = frames[this.currentFrame % this.frames.length];
		this.incrementFrame();
		return frame;
	}

	@Override
	public synchronized T getCurrentFrame() {
		return frames[this.currentFrame % this.frames.length];
	}

	private void incrementFrame() {
		if (this.currentFrame + 1 < this.frames.length || loop) {
			this.currentFrame++;
		}
	}

	@Override
	public synchronized void setCurrentFrameIndex(long newFrame) {
		if (!loop && newFrame >= this.frames.length - 1)
			this.currentFrame = this.frames.length - 1;
		else
			this.currentFrame = (int) newFrame;
	}

	@Override
	public synchronized boolean hasNextFrame() {
		return loop || this.currentFrame < this.frames.length;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.video.Video#getWidth()
	 */
	@Override
	public int getWidth()
	{
		return getCurrentFrame().getWidth();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.video.Video#getHeight()
	 */
	@Override
	public int getHeight()
	{
		return getCurrentFrame().getHeight();
	}

	@Override
	public long countFrames() {
		return this.frames.length;
	}

	@Override
	public void reset()
	{
		this.currentFrame = 0;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.video.Video#getTimeStamp()
	 */
	@Override
	public long getTimeStamp()
	{
		return (long) (1000 * getCurrentFrameIndex() / this.fps);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.video.Video#getFPS()
	 */
	@Override
	public double getFPS()
	{
		return fps;
	}
}
