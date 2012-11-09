package org.openimaj.demos.video;

import org.openimaj.demos.Demo;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;
import org.openimaj.video.processing.effects.SlitScanProcessor;

/**
 * Demo showing a slit-scan effect being applied in real-time.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
@Demo(
		author = "Sina Samangooei",
		description = "Realtime demonstration of the slit-scan " +
				"video effect. The video is rendered as temporal scan-lines, " +
				"with the newest frames appearing at the top and " +
				"oldest at the bottom.",
		keywords = { "video", "effects", "slit-scan" },
		title = "Slit-Scan Effect",
		vmArguments = "-Xmx2G")
public class SlitScanVideoDemo {
	/**
	 * The main method
	 * 
	 * @param args
	 *            ignored
	 * @throws VideoCaptureException
	 */
	public static void main(String[] args) throws VideoCaptureException {
		final VideoCapture capture = new VideoCapture(640, 480);

		VideoDisplay.createVideoDisplay(new SlitScanProcessor(capture, 240));
	}
}
