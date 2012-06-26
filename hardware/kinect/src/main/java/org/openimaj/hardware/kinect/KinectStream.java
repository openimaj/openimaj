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
package org.openimaj.hardware.kinect;

import org.openimaj.image.Image;
import org.openimaj.video.Video;


/**
 * A stream of (visual/ir/depth) data from the Kinect
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public abstract class KinectStream<T extends Image<?,T>> extends Video<T> {
	KinectController controller;
	KinectStreamCallback callback;
	T frame;
	long timeStamp;
	int width;
	int height;
	double fps;
	
	/**
	 * Construct with a reference to the controller
	 * @param controller The controller
	 */
	public KinectStream(KinectController controller) {
		this.controller = controller;
	}
	
	@Override
	public T getNextFrame() {
		currentFrame++;
		callback.swapFrames();
		return frame;
	}

	@Override
	public T getCurrentFrame() {
		return frame;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public boolean hasNextFrame() {
		return true;
	}

	@Override
	public long countFrames() {
		return this.currentFrame;
	}

	@Override
	public void reset() {
		//do nothing
	}
	
	/**
	 * Stop the stream
	 */
	public void stop() {
		callback.stop();
	}
	
	/**
	 * Get the timestamp of the current frame
	 * @return the time stamp
	 */
	@Override
	public long getTimeStamp() {
		return (long)(1000 * currentFrame / fps);
	}
	
	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.video.Video#getFPS()
	 */
	@Override
	public double getFPS()
	{
	    return fps;
	}
}
