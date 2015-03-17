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
package org.openimaj.demos.sandbox.hand;

import javax.swing.JFrame;

import jssc.SerialPort;

import org.openimaj.hardware.serial.SerialDevice;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.connectedcomponent.ConnectedComponentLabeler;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.ConnectedComponent.ConnectMode;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.video.capture.VideoCapture;

public class Fingers2 {
	public static void main(String[] args) throws Exception {
		final VideoCapture vc = new VideoCapture(320, 240);

		final JFrame frame = DisplayUtilities.displaySimple(vc.getNextFrame(), "capture");
		final ConnectedComponentLabeler ccl = new ConnectedComponentLabeler(ConnectMode.CONNECT_4);

		final String dev = "/dev/tty.usbmodemfd121";
		final SerialDevice device = new SerialDevice(dev, 9600, SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

		while (true) {
			final MBFImage cimg = vc.getNextFrame();
			final FImage gimg = cimg.flatten();
			// gimg.processInplace(new OtsuThreshold());
			gimg.threshold(0.6f);

			ccl.analyseImage(gimg);
			final ConnectedComponent hand = Fingers.findBiggest(ccl.getComponents());

			if (hand != null) {
				final Polygon poly = hand.toPolygon();
				cimg.drawPolygon(poly, 2, RGBColour.RED);

				double ratio = hand.calculateRegularBoundingBoxAspectRatio();

				System.out.println(ratio);

				ratio = 1 - ((ratio - 3.0) / (5.5 - 3));
				ratio = ratio < 0 ? 0 : ratio > 1 ? 1 : ratio;

				final int amt = (int) (ratio * 180);
				sendCommand(device, amt);
				System.out.println(ratio + " " + amt);
			}

			DisplayUtilities.display(cimg, frame);
		}
	}

	private static void sendCommand(SerialDevice device, int angle) {
		try {
			device.getOutputStream().write((angle + "\n").getBytes("US-ASCII"));
			Thread.sleep(60);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
