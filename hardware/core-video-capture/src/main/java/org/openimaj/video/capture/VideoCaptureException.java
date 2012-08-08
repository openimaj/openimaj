package org.openimaj.video.capture;

import java.io.IOException;

/**
 * Signals an exception occurred during video capture from a hardware
 * device.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class VideoCaptureException extends IOException {
	private static final long serialVersionUID = 1L;

    /**
     * Constructs an {@code VideoCaptureException} with the specified detail message.
     *
     * @param message
     *        The detail message (which is saved for later retrieval
     *        by the {@link #getMessage()} method)
     */
    public VideoCaptureException(String message) {
    	super(message);
    }
}
