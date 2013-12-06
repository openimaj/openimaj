package org.openimaj.demos.sandbox.hand;

import gnu.io.SerialPort;

import javax.swing.JFrame;

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
