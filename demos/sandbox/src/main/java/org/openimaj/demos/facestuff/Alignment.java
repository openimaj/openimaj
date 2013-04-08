package org.openimaj.demos.facestuff;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.alignment.FaceAligner;
import org.openimaj.image.processing.face.alignment.RotateScaleAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;

public class Alignment {
	public static void main(String[] args) throws MalformedURLException, IOException {
		final FImage img = ImageUtilities.readF(new URL("http://www.topnews.in/files/Barack-Obama_81.jpg"));
		final FKEFaceDetector detector = new FKEFaceDetector();
		// final FaceAligner<KEDetectedFace> aligner = new MeshWarpAligner();
		final FaceAligner<KEDetectedFace> aligner = new RotateScaleAligner();

		final KEDetectedFace face = detector.detectFaces(img).get(0);
		// DisplayUtilities.display(face.getFacePatch());

		for (final DetectedFace df : HaarCascadeDetector.BuiltInCascade.frontalface_alt.load().detectFaces(img))
			img.drawShape(df.getBounds(), 1F);

		DisplayUtilities.display(img);

		DisplayUtilities.display(aligner.align(face));
	}
}
