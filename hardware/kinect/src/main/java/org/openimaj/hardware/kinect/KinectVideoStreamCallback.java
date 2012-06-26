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

import java.nio.ByteBuffer;

import org.bridj.Pointer;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_device;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_video_cb;
import org.openimaj.image.Image;

/**
 * Abstract base class for the callback used with RGB and IR streams
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> the type of image
 */
public abstract class KinectVideoStreamCallback<T extends Image<?,T>> extends freenect_video_cb implements KinectStreamCallback {
	KinectStream<T> stream;
	ByteBuffer buffer;
	int nextTimeStamp;
	T nextFrame;
	boolean updated = false;
	
	/**
	 * Default constructor
	 * @param stream the video stream
	 */
	public KinectVideoStreamCallback(KinectStream<T> stream) {
		this.stream = stream;
	}
	
	@Override
	public synchronized void apply(Pointer<freenect_device> dev, Pointer<?> video, int timestamp) {
		updated = true;
		nextTimeStamp = timestamp;
		
		setImage();
	}
	
	/**
	 * Set the current image from the underlying buffer data
	 */
	public abstract void setImage();
	
	@Override
	public synchronized void swapFrames() {
		if (!updated) return;
		
		T tmp = stream.frame;
		stream.frame = nextFrame;
		nextFrame = tmp;
		stream.timeStamp = nextTimeStamp;
		updated = false;
	}

	@Override
	public abstract void stop();
}
