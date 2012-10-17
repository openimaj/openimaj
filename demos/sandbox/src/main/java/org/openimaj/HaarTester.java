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
package org.openimaj;

import java.io.IOException;
import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.objectdetection.filtering.OpenCVGrouping;
import org.openimaj.image.objectdetection.haar.Detector;
import org.openimaj.image.objectdetection.haar.MultiThreadedDetector;
import org.openimaj.image.objectdetection.haar.OCVHaarLoader;
import org.openimaj.image.objectdetection.haar.StageTreeClassifier;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.time.Timer;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

public class HaarTester {
	// public static void main(String[] args) throws VideoCaptureException {
	// final VideoCapture vc = new VideoCapture(640, 480);
	//
	// final VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay(vc);
	// vd.addVideoListener(new VideoDisplayListener<MBFImage>() {
	// HaarCascadeDetector det =
	// HaarCascadeDetector.BuiltInCascade.frontalface_alt2.load();
	//
	// @Override
	// public void beforeUpdate(MBFImage frame) {
	// final Timer t = Timer.timer();
	//
	// det.setMinSize(30);
	//
	// final List<DetectedFace> faces = det.detectFaces(frame.flatten());
	//
	// for (final DetectedFace f : faces) {
	// frame.drawShape(f.getBounds(), RGBColour.RED);
	// }
	//
	// System.out.println(t.duration());
	// }
	//
	// @Override
	// public void afterUpdate(VideoDisplay<MBFImage> display) {
	// // TODO Auto-generated method stub
	//
	// }
	// });
	// }

	public static void main(String[] args) throws IOException {

		final StageTreeClassifier cascade =
				OCVHaarLoader.read(StageTreeClassifier.class
						.getResourceAsStream("haarcascade_frontalface_alt2.xml"));

		final Detector det = new MultiThreadedDetector(cascade);
		det.setMinimumDetectionSize(80);

		final OpenCVGrouping grp = new OpenCVGrouping();

		final VideoCapture vc = new VideoCapture(640, 480);
		VideoDisplay.createVideoDisplay(vc).addVideoListener(new VideoDisplayListener<MBFImage>()
						{
							@Override
							public void beforeUpdate(MBFImage frame) {
								final Timer t = Timer.timer();
								List<Rectangle> rects = det.detect(frame.flatten());
								rects = grp.apply(rects);
								System.out.println(t.duration());

								for (final Rectangle r : rects) {
									frame.drawShape(r, RGBColour.RED);
								}
							}

							@Override
							public void afterUpdate(VideoDisplay<MBFImage> display) {
								// TODO Auto-generated method stub

							}
						});
	}

}
