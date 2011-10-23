package org.openimaj.hardware.kinect;

/**
 * Callback for stream data
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public interface KinectStreamCallback {
	/**
	 * swap the stream and callback buffers if the
	 * callback buffer has fresher data.
	 */
	public void swapFrames();
	
	/**
	 * Stop the stream 
	 */
	public abstract void stop();
}
