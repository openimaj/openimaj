package org.openimaj.image.contour;

import java.text.AttributedCharacterIterator.Attribute;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.contour.SuzukiContourProcessor.Border;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.convolution.FSobel;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.processing.threshold.AdaptiveLocalThresholdGaussian;
import org.openimaj.image.processing.threshold.AdaptiveLocalThresholdMean;
import org.openimaj.image.processing.threshold.OtsuThreshold;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.processor.KernelProcessor;
import org.openimaj.image.typography.FontStyle;
import org.openimaj.image.typography.FontStyle.HorizontalAlignment;
import org.openimaj.image.typography.FontStyle.VerticalAlignment;
import org.openimaj.image.typography.mathml.MathMLFont;
import org.openimaj.time.Timer;
import org.openimaj.util.queue.BoundedPriorityQueue;
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
		final ImageProcessor<FImage> thresh = new AdaptiveLocalThresholdGaussian(5, 0.05f);
//		final ImageProcessor<FImage> thresh = new AdaptiveLocalThresholdMean(20,0.05f);
//		final ImageProcessor<FImage> thresh = new CannyEdgeDetector();
//		final KernelProcessor<Float,FImage> thresh = new AdaptiveLocalThresholdMean(64);
		final Map<Attribute, Object> attrs = new HashMap<Attribute, Object>();
		attrs.put(FontStyle.FONT, new MathMLFont());
		attrs.put(FontStyle.FONT_SIZE, 12);
		attrs.put(FontStyle.COLOUR, RGBColour.GREEN);
		attrs.put(FontStyle.VERTICAL_ALIGNMENT, VerticalAlignment.VERTICAL_BOTTOM);
		attrs.put(FontStyle.HORIZONTAL_ALIGNMENT, HorizontalAlignment.HORIZONTAL_RIGHT);
		final BoundedPriorityQueue<Long> timeQueue = new BoundedPriorityQueue<Long>(10);
		
		final SuzukiContourProcessor proc = new SuzukiContourProcessor();
//		proc.setMinRelativeChildProp(0.05);
		disp.addVideoListener(new VideoDisplayListener<MBFImage>() {
			Timer timer = Timer.timer();
			@Override
			public void beforeUpdate(MBFImage frame) {
				FontStyle<Float[]> fs = FontStyle.parseAttributes(attrs, frame.createRenderer());
				final FImage img = frame.flatten();
//				DisplayUtilities.displayName(img, "Grey");
				img.processInplace(thresh);
				proc.analyseImage(img);
				final Border root = proc.root;
//				displayThreshold(img);
//				displayContours(img, root);
				List<Aestheticode> codes = new FindAestheticode().apply(root);
				for (Aestheticode ac : codes) {
					ContourRenderer.drawContours(frame, ac.root);
				}
				timeQueue.add(timer.duration());
				frame.drawText(String.format("FPS: %2.2f", 1000f/averageFrame(timeQueue)), frame.getWidth(),frame.getHeight(), fs);
				timer.start();
			}

			private void displayThreshold(final FImage img) {
				DisplayUtilities.displayName(img, "Threshold");
			}

			private void displayContours(final FImage img, final Border root) {
				final MBFImage contourFrame = new MBFImage(img.clone(), img.clone(), img.clone());
				ContourRenderer.drawContours(contourFrame, root);
				DisplayUtilities.displayName(contourFrame, "Countours");
			}

			private float averageFrame(BoundedPriorityQueue<Long> timeQueue) {
				float avg = 0;
				for (Long t : timeQueue) {
					avg += t/timeQueue.size();
				}
				return avg;
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
				// TODO Auto-generated method stub

			}
		});
	}
}
