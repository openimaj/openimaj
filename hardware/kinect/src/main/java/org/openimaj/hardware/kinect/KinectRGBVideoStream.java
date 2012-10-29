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
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_device;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_resolution;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_video_format;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;

/**
 * Callback for rgb camera data
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
class RGBVideoCallback extends KinectVideoStreamCallback<MBFImage> {
	public RGBVideoCallback(KinectStream<MBFImage> stream) {
		super(stream);
		final Pointer<freenect_device> device = stream.controller.device;

		libfreenectLibrary.freenect_set_video_mode_proxy(device, freenect_resolution.FREENECT_RESOLUTION_MEDIUM,
				freenect_video_format.FREENECT_VIDEO_RGB);

		buffer = ByteBuffer.allocateDirect(libfreenectLibrary.freenect_get_video_buffer_size(device));
		libfreenectLibrary.freenect_set_video_buffer(device, Pointer.pointerToBuffer(buffer));

		nextFrame = new MBFImage(stream.width, stream.height, ColourSpace.RGB);

		libfreenectLibrary.freenect_set_video_callback(device, toPointer());
		libfreenectLibrary.freenect_start_video(device);
	}

	@Override
	public void setImage() {
		final ByteBuffer buf = buffer.duplicate();

		final int width = stream.width;
		final int height = stream.height;

		final float[][] r = nextFrame.bands.get(0).pixels;
		final float[][] g = nextFrame.bands.get(1).pixels;
		final float[][] b = nextFrame.bands.get(2).pixels;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final int red = buf.get() & 0xFF;
				final int green = buf.get() & 0xFF;
				final int blue = buf.get() & 0xFF;
				r[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[red];
				g[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[green];
				b[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[blue];
			}
		}
	}

	@Override
	public void stop() {
		libfreenectLibrary.freenect_stop_video(stream.controller.device);
	}
}

/**
 * The stream of RGB information
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class KinectRGBVideoStream extends KinectStream<MBFImage> {
	/**
	 * Construct with a reference to the controller
	 * 
	 * @param controller
	 *            The controller
	 */
	public KinectRGBVideoStream(KinectController controller) {
		super(controller);

		fps = 30;
		width = 640;
		height = 480;
		frame = new MBFImage(width, height, ColourSpace.RGB);

		callback = new RGBVideoCallback(this);
	}
}
