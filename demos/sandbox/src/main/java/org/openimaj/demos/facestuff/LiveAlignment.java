package org.openimaj.demos.facestuff;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.alignment.FaceAligner;
import org.openimaj.image.processing.face.alignment.RotateScaleAligner;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

public class LiveAlignment {
	public static void main(String[] args) throws MalformedURLException, IOException {
		final FKEFaceDetector detector = new FKEFaceDetector();
		// final FaceAligner<KEDetectedFace> aligner = new MeshWarpAligner();
		final FaceAligner<KEDetectedFace> aligner = new RotateScaleAligner(200);

		final VideoCapture vc = new VideoCapture(640, 480);
		VideoDisplay.createOffscreenVideoDisplay(vc).addVideoListener(new VideoDisplayListener<MBFImage>() {

			@Override
			public void beforeUpdate(MBFImage frame) {
				if (frame == null)
					return;
				final List<KEDetectedFace> faces = detector.detectFaces(frame.flatten());

				if (faces.size() <= 0)
					return;

				final KEDetectedFace face = faces.get(0);
				DisplayUtilities.displayName(aligner.align(face), "aligned");
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
			}
		});
	}
}
