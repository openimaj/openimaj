package org.openimaj.demos.sandbox.image;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.alignment.CLMAligner;
import org.openimaj.image.processing.face.detection.CLMDetectedFace;
import org.openimaj.image.processing.face.detection.CLMFaceDetector;

public class CLMAlignerTest {
	public static void main(String[] args) throws IOException {
		final FImage image = ImageUtilities.readF(new File("/Users/jsh2/Desktop/face.jpg"));

		final CLMFaceDetector detector = new CLMFaceDetector();
		final List<CLMDetectedFace> faces = detector.detectFaces(image);

		DisplayUtilities.display(faces.get(0).getFacePatch());

		final CLMAligner aligner = new CLMAligner();

		DisplayUtilities.display(aligner.align(faces.get(0)));
	}
}
