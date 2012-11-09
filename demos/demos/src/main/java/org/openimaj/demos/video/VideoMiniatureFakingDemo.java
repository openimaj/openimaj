package org.openimaj.demos.video;

import org.openimaj.demos.Demo;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.effects.DioramaEffect;
import org.openimaj.image.processor.ProcessorUtilities;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;
import org.openimaj.video.processor.VideoFrameProcessor;

/**
 * Real-time miniature faking.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Demo(
		author = "Jonathon Hare",
		description = "Realtime demo of miniature faking (the 'Diorama Effect').",
		keywords = { "video", "effect", "diorama", "miniature faking" },
		title = "Miniature Faking")
public class VideoMiniatureFakingDemo {
	/**
	 * Main method
	 * 
	 * @param args
	 *            ignored
	 * @throws VideoCaptureException
	 */
	public static void main(String[] args) throws VideoCaptureException {
		final int w = 640;
		final int h = 480;
		final Line2d axis = new Line2d(w / 2, h / 2, w / 2, h);

		VideoDisplay.createVideoDisplay(new VideoFrameProcessor<MBFImage>(new VideoCapture(w, h),
				ProcessorUtilities.wrap(new DioramaEffect(axis))));
	}
}
