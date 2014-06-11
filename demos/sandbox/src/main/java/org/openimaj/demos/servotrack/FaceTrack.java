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
package org.openimaj.demos.servotrack;

import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

public class FaceTrack {
	public static void main(String[] args) throws Exception {
		final VideoCapture capture = new VideoCapture(640, 480, VideoCapture.getVideoDevices().get(0));
		final PTServoController servos = new PTServoController("/dev/tty.usbmodemfa131");
		final HaarCascadeDetector faceDetector = HaarCascadeDetector.BuiltInCascade.frontalface_alt2.load();
		faceDetector.setMinSize(80);
		final Point2d frameCentre = new Point2dImpl(capture.getWidth() / 2, capture.getHeight() / 2);

		VideoDisplay.createVideoDisplay(capture).addVideoListener(new VideoDisplayListener<MBFImage>() {

			@Override
			public void beforeUpdate(MBFImage frame) {
				if (frame == null)
					return;

				final List<DetectedFace> faces = faceDetector.detectFaces(frame.flatten());

				if (faces == null || faces.size() == 0) {
					// move back towards center
					final int tilt = (90 - servos.getTilt()) / 5;
					servos.changeTiltBy(tilt);

					final int pan = (90 - servos.getPan()) / 5;
					servos.changePanBy(pan);
				} else {
					frame.drawShape(faces.get(0).getBounds(), RGBColour.RED);

					// move towards face
					final Point2d pt = faces.get(0).getBounds().calculateCentroid();

					final Point2d delta = pt.minus(frameCentre);

					final double damp = 0.03;

					if (delta.getX() < 0) {
						servos.changePanBy(-(int) (damp * delta.getX()));
					} else if (delta.getX() > 0) {
						servos.changePanBy(-(int) (damp * delta.getX()));
					}

					if (delta.getY() < 0) {
						servos.changeTiltBy((int) (damp * delta.getY()));
					} else if (delta.getY() > 0) {
						servos.changeTiltBy((int) (damp * delta.getY()));
					}
				}

				// try {
				// Thread.sleep(1500);
				// } catch (final InterruptedException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
				// do nothing
			}
		});

	}
}
