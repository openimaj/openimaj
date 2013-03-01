package org.openimaj.demos;

import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

public class VideoTest {
	public static void main(String[] args) throws VideoCaptureException {
		VideoCapture c = new VideoCapture(640, 480);
		VideoDisplay.createVideoDisplay(c);
	}
}
