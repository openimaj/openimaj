package org.openimaj.hardware.kinect;

public class KinectException extends Exception {
	private static final long serialVersionUID = 1L;

	public KinectException() {
		super();
	}

	public KinectException(String message, Throwable cause) {
		super(message, cause);
	}

	public KinectException(String message) {
		super(message);
	}

	public KinectException(Throwable cause) {
		super(cause);
	}
}
