package org.openimaj.hardware.kinect;

import org.openimaj.image.Image;
import org.openimaj.video.Video;


/**
 * A stream of (visual/ir/depth) data from the Kinect
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T>
 */
abstract class KinectStream<T extends Image<?,T>> extends Video<T> {
	KinectController controller;
	KinectStreamCallback callback;
	T frame;
	int timeStamp;
	int width;
	int height;
	
	public KinectStream(KinectController controller) {
		this.controller = controller;
	}
	
	@Override
	public T getNextFrame() {
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
		return -1;
	}

	@Override
	public void reset() {
		//do nothing
	}
	
	public void stop() {
		callback.stop();
	}
	
	/**
	 * Get the timestamp of the current frame
	 * @return the time stamp
	 */
	public int getTimeStamp() {
		return timeStamp;
	}
}
