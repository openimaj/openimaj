package org.openimaj.demos.facestuff;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.alignment.FaceAligner;
import org.openimaj.image.processing.face.alignment.MeshWarpAligner;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.FacialKeypoint;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.math.geometry.transforms.TransformUtilities;

public class Alignment {
	public static void main(String[] args) throws MalformedURLException, IOException {
		final FImage img = ImageUtilities.readF(new URL("http://www.topnews.in/files/Barack-Obama_81.jpg"));
		final FKEFaceDetector detector = new FKEFaceDetector();
		final FaceAligner<KEDetectedFace> aligner = new MeshWarpAligner();
		// final FaceAligner<KEDetectedFace> aligner = new
		// RotateScaleAligner(200);

		final KEDetectedFace face = detector.detectFaces(img).get(0);
		// DisplayUtilities.display(face.getFacePatch());

		for (final FacialKeypoint kpt : face.getKeypoints())
			img.drawPoint(
					kpt.position.transform(TransformUtilities.translateMatrix(face.getBounds().x, face.getBounds().y)),
					1f, 3);

		img.drawShape(new HaarCascadeDetector(80).detectFaces(img).get(0).getBounds(), 1F);
		img.drawShape(face.getBounds(), 0.8F);

		DisplayUtilities.display(img);
		DisplayUtilities.display(aligner.align(face));
	}
}
