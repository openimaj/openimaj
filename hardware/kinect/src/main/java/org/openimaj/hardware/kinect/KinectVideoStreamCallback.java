package org.openimaj.hardware.kinect;

import java.nio.ByteBuffer;

import org.bridj.Pointer;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_device;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_video_cb;
import org.openimaj.image.Image;

/**
 * Abstract base class for the callback used with RGB and IR streams
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
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
