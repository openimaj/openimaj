package org.openimaj.image.objectdetection.haar.training;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.objectdetection.haar.Detector;
import org.openimaj.image.objectdetection.haar.StageTreeClassifier;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

public class Runner {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		final StageTreeClassifier classifier = IOUtils.read(new ObjectInputStream(new FileInputStream(new File(
				"test-classifier.bin"))));

		// final StageTreeClassifier classifier =
		// OCVHaarLoader.read(OCVHaarLoader.class
		// .getResourceAsStream("test.xml"));

		final Detector d = new Detector(classifier);
		d.setMinimumDetectionSize(100);

		final VideoCapture vc = new VideoCapture(640, 480);
		VideoDisplay.createVideoDisplay(vc).addVideoListener(new VideoDisplayListener<MBFImage>() {

			@Override
			public void beforeUpdate(MBFImage frame) {
				final List<Rectangle> rects = d.detect(frame.flatten());

				System.out.println(rects.size());

				for (final Rectangle r : rects)
					frame.drawShape(r, RGBColour.RED);
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
				// TODO Auto-generated method stub

			}
		});
	}
}
