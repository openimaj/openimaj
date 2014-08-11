package org.openimaj.video.gstreamer;

import java.io.IOException;

/**
 * Signals an exception occurred during video capture from a GStreamer pipeline.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class GStreamerException extends IOException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an {@code VideoCaptureException} with the specified detail
	 * message.
	 * 
	 * @param message
	 *            The detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method)
	 */
	public GStreamerException(String message) {
		super(message);
	}
}
