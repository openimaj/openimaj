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
package org.openimaj.demos.sandbox;

import java.io.IOException;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.objectdetection.FilteringObjectDetector;
import org.openimaj.image.objectdetection.RotationSimulationObjectDetector;
import org.openimaj.image.objectdetection.TransformedDetection;
import org.openimaj.image.objectdetection.filtering.OpenCVGrouping;
import org.openimaj.image.objectdetection.haar.Detector;
import org.openimaj.image.objectdetection.haar.OCVHaarLoader;
import org.openimaj.image.objectdetection.haar.StageTreeClassifier;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.time.Timer;
import org.openimaj.util.pair.ObjectIntPair;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

public class HaarTester {
	public static void main(String[] args) throws IOException {

		final StageTreeClassifier cascade =
				OCVHaarLoader.read(StageTreeClassifier.class
						.getResourceAsStream("haarcascade_frontalface_alt.xml"));

		final Detector haar = new Detector(cascade);
		haar.setMinimumDetectionSize(20);

		final RotationSimulationObjectDetector<FImage, Float, ObjectIntPair<Rectangle>> det =
				new RotationSimulationObjectDetector<FImage, Float, ObjectIntPair<Rectangle>>(
						new FilteringObjectDetector<FImage, Rectangle, ObjectIntPair<Rectangle>>(haar,
								new OpenCVGrouping(3)), new float[] { -0.7f, -0.35f, 0f, 0.35f, 0.7f }, 0.25f);

		final VideoCapture vc = new VideoCapture(640, 480);
		VideoDisplay.createVideoDisplay(vc).addVideoListener(new VideoDisplayListener<MBFImage>()
		{
			@Override
			public void beforeUpdate(MBFImage frame) {
				final Timer t = Timer.timer();

				final FImage detectionImage = frame.flatten();

				final List<TransformedDetection<ObjectIntPair<Rectangle>>> rects = det.detect(detectionImage);
				// rects = grp.apply(rects);
				System.out.println(t.duration());

				for (final TransformedDetection<ObjectIntPair<Rectangle>> r : rects) {
					final Shape td = r.detected.first.transform(r.transform);

					System.out.println(td.calculateRegularBoundingBox());

					frame.drawShape(td, RGBColour.RED);
				}
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
				// TODO Auto-generated method stub

			}
		});
	}
}
