package org.openimaj.image.contour;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.contour.SuzukiContourProcessor.Border;
import org.openimaj.image.processing.threshold.OtsuThreshold;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class ContourVideo {

	public static void main(String[] args) throws VideoCaptureException {
		final VideoCapture cap = new VideoCapture(640, 480);
		final VideoDisplay<MBFImage> disp = VideoDisplay.createVideoDisplay(cap);
		final OtsuThreshold thresh = new OtsuThreshold();
		disp.addVideoListener(new VideoDisplayListener<MBFImage>() {

			@Override
			public void beforeUpdate(MBFImage frame) {
				final FImage img = frame.flatten();
				DisplayUtilities.displayName(img, "Grey");
				img.processInplace(thresh);
				DisplayUtilities.displayName(img, "Threshold");
				final MBFImage contourFrame = new MBFImage(img.clone(), img.clone(), img.clone());
				final Border root = SuzukiContourProcessor.findContours(img);
				ContourRenderer.drawContours(contourFrame, root);
				DisplayUtilities.displayName(contourFrame, "Countours");
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
				// TODO Auto-generated method stub

			}
		});
	}
}
