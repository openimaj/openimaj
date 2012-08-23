package org.openimaj.demos.sandbox;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.SwingUtilities;

import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

public class VideoTest implements KeyListener {
	VideoDisplay<MBFImage> display;

	VideoTest() throws VideoCaptureException {
		display = VideoDisplay.createVideoDisplay(new VideoCapture(320, 240));
		SwingUtilities.getRoot(display.getScreen()).addKeyListener(this);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyChar() == ' ')
			display.togglePause();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) throws VideoCaptureException {
		new VideoTest();
	}
}
