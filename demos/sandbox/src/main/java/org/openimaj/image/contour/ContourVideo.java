package org.openimaj.image.contour;

import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.contour.ContourAestheticode.Aestheticode;
import org.openimaj.image.contour.SuzukiContourProcessor.Border;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.threshold.AdaptiveLocalThresholdMean;
import org.openimaj.image.processing.threshold.OtsuThreshold;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.processor.KernelProcessor;
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
		final ImageProcessor<FImage> thresh = new OtsuThreshold();
//		final KernelProcessor<Float,FImage> thresh = new AdaptiveLocalThresholdMean(64);
		disp.addVideoListener(new VideoDisplayListener<MBFImage>() {

			@Override
			public void beforeUpdate(MBFImage frame) {
				final FImage img = frame.flatten();
				img.processInplace(new FGaussianConvolve(20));
				DisplayUtilities.displayName(img, "Grey");
//				img.processInplace(thresh);
////				DisplayUtilities.displayName(img, "Threshold");
//				final MBFImage contourFrame = new MBFImage(img.clone(), img.clone(), img.clone());
//				final MBFImage aestheticodeFrame = new MBFImage(img.clone(), img.clone(), img.clone());
//				final Border root = SuzukiContourProcessor.findContours(img);
//				ContourRenderer.drawContours(contourFrame, root);
////				DisplayUtilities.displayName(contourFrame, "Countours");
//				List<Aestheticode> codes = new FindAestheticode().apply(root);
//				for (Aestheticode ac : codes) {
//					ContourRenderer.drawContours(aestheticodeFrame, ac.root);
//				}
//				DisplayUtilities.displayName(aestheticodeFrame, "Aestheticodes");
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
				// TODO Auto-generated method stub

			}
		});
	}
}
