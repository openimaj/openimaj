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
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_depth_cb;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_depth_format;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_device;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_resolution;
import org.openimaj.image.FImage;

/**
 * Callback handling the depth information
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
class DepthCallback extends freenect_depth_cb implements KinectStreamCallback {
	private static final float[] LUT = new float[2048];
	static {
		for (int i = 0; i < LUT.length; i++)
			LUT[i] = i / (LUT.length - 1f);
	}

	KinectDepthStream stream;
	ByteBuffer buffer;
	int nextTimeStamp;
	FImage nextFrame;
	boolean updated = false;

	public DepthCallback(KinectDepthStream stream, boolean registeredDepthMode) {
		this.stream = stream;
		final Pointer<freenect_device> device = stream.controller.device;

		if (registeredDepthMode)
			libfreenectLibrary.freenect_set_depth_mode_proxy(device, freenect_resolution.FREENECT_RESOLUTION_MEDIUM,
					freenect_depth_format.FREENECT_DEPTH_REGISTERED);
		else
			libfreenectLibrary.freenect_set_depth_mode_proxy(device, freenect_resolution.FREENECT_RESOLUTION_MEDIUM,
					freenect_depth_format.FREENECT_DEPTH_11BIT);

		buffer = ByteBuffer.allocateDirect(libfreenectLibrary.freenect_get_video_buffer_size(device));
		libfreenectLibrary.freenect_set_depth_buffer(device, Pointer.pointerToBuffer(buffer));

		nextFrame = new FImage(stream.width, stream.height);

		libfreenectLibrary.freenect_set_depth_callback(device, toPointer());
		libfreenectLibrary.freenect_start_depth(device);
	}

	@Override
	public synchronized void apply(Pointer<freenect_device> dev, Pointer<?> depth, int timestamp) {
		updated = true;
		nextTimeStamp = timestamp;

		setImage();
	}

	@Override
	public synchronized void swapFrames() {
		if (!updated)
			return;

		final FImage tmp = stream.frame;
		stream.frame = nextFrame;
		nextFrame = tmp;
		stream.timeStamp = nextTimeStamp;
		updated = false;
	}

	public void setImage() {
		final ByteBuffer buf = buffer.duplicate();

		final int width = stream.width;
		final int height = stream.height;

		final float[][] pix = nextFrame.pixels;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final int first = (buf.get() & 0xFF);
				final int second = (buf.get() & 0xFF);
				pix[y][x] = first + (second << 8);
			}
		}
	}

	@Override
	public void stop() {
		libfreenectLibrary.freenect_stop_depth(stream.controller.device);
	}
}

/**
 * The stream of depth information
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class KinectDepthStream extends KinectStream<FImage> {
	boolean registered;

	/**
	 * Construct with a reference to the controller
	 * 
	 * @param controller
	 *            The controller
	 * @param registeredDepthMode
	 *            whether to register the depth image. If true, depth
	 *            measurements are in millimeters.
	 */
	public KinectDepthStream(KinectController controller, boolean registeredDepthMode) {
		super(controller);

		fps = 30;
		width = 640;
		height = 480;
		frame = new FImage(width, height);
		registered = registeredDepthMode;

		callback = new DepthCallback(this, registeredDepthMode);
	}
}
