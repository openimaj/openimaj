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
	long timeStamp;
	int width;
	int height;
	double fps;
	
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
	 *  @inheritDoc
	 *  @see org.openimaj.video.Video#getFPS()
	 */
	@Override
	public double getFPS()
	{
	    return fps;
	}
}
