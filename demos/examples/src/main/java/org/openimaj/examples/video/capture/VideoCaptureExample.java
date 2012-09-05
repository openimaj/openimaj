package org.openimaj.examples.video.capture;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.SwingUtilities;

import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

/**
 * Example showing how to open the default video capture device and display it
 * in a window. Also shows how to hook up a key listener so that the capture
 * pauses/unpauses when the spacebar is pressed.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class VideoCaptureExample extends KeyAdapter {
	VideoDisplay<MBFImage> display;

	VideoCaptureExample() throws VideoCaptureException {
		// open the capture device and create a window to display in
		display = VideoDisplay.createVideoDisplay(new VideoCapture(320, 240));

		// set the key listener of the display to this
		SwingUtilities.getRoot(display.getScreen()).addKeyListener(this);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// space pauses the video
		if (e.getKeyChar() == ' ')
			display.togglePause();
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 *            ignored
	 * @throws VideoCaptureException
	 *             if their is a problem with the video capture hardware
	 */
	public static void main(String[] args) throws VideoCaptureException {
		new VideoCaptureExample();
	}
}
