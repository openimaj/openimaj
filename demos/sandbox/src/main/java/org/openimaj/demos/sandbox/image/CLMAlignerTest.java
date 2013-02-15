package org.openimaj.demos.sandbox.image;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.alignment.CLMAligner;
import org.openimaj.image.processing.face.detection.CLMDetectedFace;
import org.openimaj.image.processing.face.detection.CLMFaceDetector;
import org.openimaj.math.geometry.shape.Rectangle;

public class CLMAlignerTest {
	public static void main(String[] args) throws IOException {
		final FImage image = ImageUtilities.readF(new File("/Users/jsh2/Desktop/test-images/A7K9ZlZCAAA9VoL.jpg"));

		final CLMFaceDetector detector = new CLMFaceDetector();
		final List<Rectangle> rects = detector.getConfiguration().faceDetector.detect(image);

		final MBFImage img = new MBFImage(image.clone(), image.clone(), image.clone());
		for (final Rectangle r : rects) {
			r.scaleCOG(1.2f);
			img.drawShape(r, RGBColour.RED);
		}
		DisplayUtilities.display(img);

		final List<CLMDetectedFace> faces = detector.detectFaces(image, rects);

		final CLMAligner aligner = new CLMAligner();

		DisplayUtilities.display(aligner.align(faces.get(0)));
	}
}
