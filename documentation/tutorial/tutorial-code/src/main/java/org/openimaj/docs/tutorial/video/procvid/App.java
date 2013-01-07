package org.openimaj.docs.tutorial.video.procvid;

import java.io.IOException;
import java.net.URL;

import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.xuggle.XuggleVideo;

/**
 * OpenIMAJ Hello world!
 * 
 */
public class App {
	/**
	 * Main method
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final Video<MBFImage> video;
		video = new XuggleVideo(new URL("http://dl.dropbox.com/u/8705593/keyboardcat.flv"));
		// video = new VideoCapture(320, 240);

		// final VideoDisplay<MBFImage> display =
		// VideoDisplay.createVideoDisplay(video);
		// for (final MBFImage mbfImage : video) {
		// DisplayUtilities.displayName(mbfImage.process(new
		// CannyEdgeDetector()), "videoFrames");
		// }

		final VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(video);
		display.addVideoListener(
				new VideoDisplayListener<MBFImage>() {
					@Override
					public void beforeUpdate(MBFImage frame) {
						frame.processInplace(new CannyEdgeDetector());
					}

					@Override
					public void afterUpdate(VideoDisplay<MBFImage> display) {
					}
				});
	}
}
