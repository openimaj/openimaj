package org.openimaj.hardware.kinect;

import java.nio.ByteBuffer;

import org.bridj.Pointer;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_device;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_resolution;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_video_format;
import org.openimaj.image.FImage;

/**
 * Callback for IR data
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
class IRVideoCallback extends KinectVideoStreamCallback<FImage> {
	private static final float[] LUT = new float[1024];
	static {
		for (int i=0; i<LUT.length; i++) LUT[i] = (float)i / (LUT.length - 1f);
	}
	
	public IRVideoCallback(KinectStream<FImage> stream) {
		super(stream);
		final Pointer<freenect_device> device = stream.controller.device;
		
		libfreenectLibrary.freenect_set_video_mode_proxy(device, freenect_resolution.FREENECT_RESOLUTION_MEDIUM, freenect_video_format.FREENECT_VIDEO_IR_10BIT);
		
		buffer = ByteBuffer.allocateDirect(libfreenectLibrary.freenect_get_video_buffer_size(device));
		libfreenectLibrary.freenect_set_video_buffer(device, Pointer.pointerToBuffer(buffer));
		
		nextFrame = new FImage(stream.width, stream.height);
		
		libfreenectLibrary.freenect_set_video_callback(device, toPointer());
		libfreenectLibrary.freenect_start_video(device);
	}
	
	@Override
	public void setImage() {
		ByteBuffer buf = buffer.duplicate();
		
		final int width = stream.width;
		final int height = stream.height;
		
		final float[][] pix = nextFrame.pixels;
		
		for (int i=0, y=0; y<height; y++) {
			for (int x=0; x<width; x++, i+=3) {
				int first = (buf.get() & 0xFF);
				int second = (buf.get() & 0xFF);
				pix[y][x] = LUT[first + second * 256];
			}
		}
	}

	@Override
	public void stop() {
		libfreenectLibrary.freenect_stop_video(stream.controller.device);
	}
}

/**
 * The stream of IR information
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class KinectIRVideoStream extends KinectStream<FImage> {
	public KinectIRVideoStream(KinectController controller) {
		super(controller);
		
		fps = 30;
		width = 640;
		height = 480;
		frame = new FImage(width, height);
		
		callback = new IRVideoCallback(this);
	}
}
