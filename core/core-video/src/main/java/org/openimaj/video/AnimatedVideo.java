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
 * A basic abstract implementation of a video that displays an image and
 * provides double-buffering
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <I>
 */
public abstract class AnimatedVideo<I extends Image<?, I>> extends Video<I>
{
	private I currentFrame;
	private I nextFrame;
	private double fps;

	/**
	 * Default video constructor with a rate of 30 fps using the given image as
	 * a basis.
	 * 
	 * @param blankFrame
	 *            blank video frame to pass to the update method
	 */
	public AnimatedVideo(I blankFrame) {
		this(blankFrame, 30);
	}

	/**
	 * Default video constructor with the given rate using the given image as a
	 * basis.
	 * 
	 * @param blankFrame
	 *            blank video frame to pass to the update method
	 * @param fps
	 *            the frame rate
	 */
	public AnimatedVideo(I blankFrame, double fps) {
		currentFrame = blankFrame;
		nextFrame = blankFrame.clone();
		this.fps = fps;

		init();
	}

	protected abstract void updateNextFrame(I frame);

	@Override
	public I getNextFrame() {
		updateNextFrame(nextFrame);
		final I tmp = currentFrame;
		currentFrame = nextFrame;
		nextFrame = tmp;

		super.currentFrame++;

		return currentFrame;
	}

	@Override
	public I getCurrentFrame() {
		return currentFrame;
	}

	@Override
	public int getWidth() {
		return currentFrame.getWidth();
	}

	@Override
	public int getHeight() {
		return currentFrame.getHeight();
	}

	@Override
	public long getTimeStamp() {
		return (long) (super.currentFrame * 1000 / this.fps);
	}

	@Override
	public double getFPS() {
		return fps;
	}

	@Override
	public boolean hasNextFrame() {
		return true;
	}

	@Override
	public long countFrames() {
		return -1;
	}

	@Override
	public void reset() {
		super.currentFrame = 0;
	}

	/**
	 * Called by the constructor. Does nothing by default.
	 */
	protected void init() {
		// do nothing by default, but might be overridden to do things with
		// animators
	}
}
