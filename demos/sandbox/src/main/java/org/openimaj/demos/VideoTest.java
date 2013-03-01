package org.openimaj.demos;

import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

public class VideoTest {
	public static void main(String[] args) throws VideoCaptureException {
		if (args.length == 0) {
			int i = 0;

			System.out.println("Usage: VideoTest device [width [height [rate]]]");

			for (final Device d : VideoCapture.getVideoDevices()) {
				System.out.println(i + "\t" + d.getNameStr());
				i++;
			}

			return;
		}

		final Device dev = VideoCapture.getVideoDevices().get(Integer.parseInt(args[0]));
		final int width = args.length > 2 ? Integer.parseInt(args[1]) : 320;
		final int height = args.length > 2 ? Integer.parseInt(args[2]) : 240;
		final double rate = args.length > 2 ? Double.parseDouble(args[3]) : 30;

		final VideoCapture c = new VideoCapture(width, height, rate, dev);
		VideoDisplay.createVideoDisplay(c);
	}
}
