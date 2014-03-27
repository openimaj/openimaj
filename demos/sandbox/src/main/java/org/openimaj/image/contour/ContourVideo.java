/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
import org.openimaj.image.feature.astheticode.Aestheticode;
import org.openimaj.image.feature.astheticode.FindAestheticode;
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
		final VideoCapture cap = new VideoCapture(320, 240);
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
				displayThreshold(img);
				displayContours(img, root);
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
