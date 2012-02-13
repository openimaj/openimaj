package org.openimaj.hardware.kinect;

/**
 * Checked exceptions for the Kinect driver.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class KinectException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public KinectException() {
		super();
	}

	/**
	 * Construct with a message and cause.
	 * @param message The message.
	 * @param cause The underlying cause of the exception.
	 */
	public KinectException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Construct with a message.
	 * @param message The message.
	 */
	public KinectException(String message) {
		super(message);
	}

	/**
	 * Construct with an underlying cause.
	 * @param cause The underlying cause of the exception.
	 */
	public KinectException(Throwable cause) {
		super(cause);
	}
}
