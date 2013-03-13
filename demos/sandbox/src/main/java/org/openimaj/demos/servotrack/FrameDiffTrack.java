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
