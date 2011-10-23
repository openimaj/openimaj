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
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
class RGBVideoCallback extends KinectVideoStreamCallback<MBFImage> {
	public RGBVideoCallback(KinectStream<MBFImage> stream) {
		super(stream);
		final Pointer<freenect_device> device = stream.controller.device;
		
		libfreenectLibrary.freenect_set_video_mode_proxy(device, freenect_resolution.FREENECT_RESOLUTION_MEDIUM, freenect_video_format.FREENECT_VIDEO_RGB);
		
		buffer = ByteBuffer.allocateDirect(libfreenectLibrary.freenect_get_video_buffer_size(device));
		libfreenectLibrary.freenect_set_video_buffer(device, Pointer.pointerToBuffer(buffer));
		
		nextFrame = new MBFImage(stream.width, stream.height, ColourSpace.RGB);
		
		libfreenectLibrary.freenect_set_video_callback(device, toPointer());
		libfreenectLibrary.freenect_start_video(device);
	}
	
	@Override
	public void setImage() {
		ByteBuffer buf = buffer.duplicate();
		
		final int width = stream.width;
		final int height = stream.height;
		
		final float[][] r = nextFrame.bands.get(0).pixels;
		final float[][] g = nextFrame.bands.get(1).pixels;
		final float[][] b = nextFrame.bands.get(2).pixels;
		
		for (int i=0, y=0; y<height; y++) {
			for (int x=0; x<width; x++, i+=3) {
				int red = buf.get() & 0xFF;
				int green = buf.get() & 0xFF;
				int blue = buf.get() & 0xFF;
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
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class KinectRGBVideoStream extends KinectStream<MBFImage> {
	public KinectRGBVideoStream(KinectController controller) {
		super(controller);
		
		fps = 30;
		width = 640;
		height = 480;
		frame = new MBFImage(width, height, ColourSpace.RGB);
		
		callback = new RGBVideoCallback(this);
	}
}
