package org.openimaj.demos.servotrack;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.image.saliency.AchantaSaliency;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

public class SaliencyTrack {
	public static void main(String[] args) throws Exception {
		final VideoCapture capture = new VideoCapture(640, 480, VideoCapture.getVideoDevices().get(0));
		final PTServoController servos = new PTServoController("/dev/tty.usbmodemfa131");
		final Point2d frameCentre = new Point2dImpl(capture.getWidth() / 2, capture.getHeight() / 2);

		VideoDisplay.createVideoDisplay(capture).addVideoListener(new VideoDisplayListener<MBFImage>() {
			AchantaSaliency sal = new AchantaSaliency();

			@Override
			public void beforeUpdate(MBFImage frame) {
				sal.analyseImage(frame);
				final FValuePixel pt = sal.getSaliencyMap().maxPixel();

				frame.internalAssign(new MBFImage(sal.getSaliencyMap(), sal.getSaliencyMap(), sal.getSaliencyMap()));
				frame.drawPoint(pt, RGBColour.RED, 5);

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

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
				// do nothing
			}
		});

	}
}
