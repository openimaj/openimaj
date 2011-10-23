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
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
class DepthCallback extends freenect_depth_cb implements KinectStreamCallback {
	private static final float[] LUT = new float[2048];
	static {
		for (int i=0; i<LUT.length; i++) LUT[i] = (float)i / (LUT.length - 1f);
	}
	
	KinectDepthStream stream;
	ByteBuffer buffer;
	int nextTimeStamp;
	FImage nextFrame;
	boolean updated = false;
	
	public DepthCallback(KinectDepthStream stream) {
		this.stream = stream;
		final Pointer<freenect_device> device = stream.controller.device;
		
		libfreenectLibrary.freenect_set_depth_mode_proxy(device, freenect_resolution.FREENECT_RESOLUTION_MEDIUM, freenect_depth_format.FREENECT_DEPTH_11BIT);
		
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
		if (!updated) return;
		
		FImage tmp = stream.frame;
		stream.frame = nextFrame;
		nextFrame = tmp;
		stream.timeStamp = nextTimeStamp;
		updated = false;
	}
	
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

	public void stop() {
		libfreenectLibrary.freenect_stop_video(stream.controller.device);
	}
}

/**
 * The stream of depth information
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class KinectDepthStream extends KinectStream<FImage> {
	public KinectDepthStream(KinectController controller) {
		super(controller);
		
		fps = 30;
		width = 640;
		height = 480;
		frame = new FImage(width, height);
		
		callback = new DepthCallback(this);
	}
}
