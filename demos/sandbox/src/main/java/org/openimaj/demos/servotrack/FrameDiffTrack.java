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

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.connectedcomponent.ConnectedComponentLabeler;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.ConnectedComponent.ConnectMode;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

public class FrameDiffTrack {
	public static void main(String[] args) throws Exception {
		final VideoCapture capture = new VideoCapture(640, 480, VideoCapture.getVideoDevices().get(0));
		final PTServoController servos = new PTServoController("/dev/tty.usbmodemfa131");
		final Point2d frameCentre = new Point2dImpl(capture.getWidth() / 2, capture.getHeight() / 2);

		VideoDisplay.createVideoDisplay(capture).addVideoListener(new VideoDisplayListener<MBFImage>() {
			FImage previousFrame = capture.getCurrentFrame().flatten();
			int inhibit = 0;

			@Override
			public void beforeUpdate(MBFImage frame) {
				final FImage thisFrame = frame.flatten();
				final FImage delta = thisFrame.subtract(previousFrame).abs().threshold(0.3f);

				final ConnectedComponentLabeler ccl = new ConnectedComponentLabeler(ConnectMode.CONNECT_8);
				final List<ConnectedComponent> comps = ccl.findComponents(delta);

				if (inhibit <= 0 && comps.size() > 0) {
					ConnectedComponent big = comps.get(0);

					for (int i = 1; i < comps.size(); i++) {
						final ConnectedComponent cc = comps.get(i);
						if (cc.calculateArea() > big.calculateArea())
							big = cc;
					}

					if (big.calculateArea() > 500) {
						frame.drawShape(big.toPolygon(), RGBColour.RED);

						final Point2d pt = big.calculateCentroidPixel();

						final Point2d deltap = pt.minus(frameCentre);

						final double damp = 0.03;

						if (deltap.getX() < 0) {
							servos.changePanBy(-(int) (damp * deltap.getX()));
						} else if (deltap.getX() > 0) {
							servos.changePanBy(-(int) (damp * deltap.getX()));
						}

						if (deltap.getY() < 0) {
							servos.changeTiltBy((int) (damp * deltap.getY()));
						} else if (deltap.getY() > 0) {
							servos.changeTiltBy((int) (damp * deltap.getY()));
						}

						inhibit = 10;
					}
				}

				previousFrame = thisFrame;
				inhibit--;
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
				// do nothing
			}
		});

	}
}
