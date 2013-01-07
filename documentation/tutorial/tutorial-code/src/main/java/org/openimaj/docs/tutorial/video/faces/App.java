package org.openimaj.docs.tutorial.video.faces;

import java.io.IOException;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

/**
 * OpenIMAJ Hello world!
 * 
 */
public class App {
	/**
	 * Main method
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final VideoCapture vc = new VideoCapture(320, 240);
		final VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay(vc);
		vd.addVideoListener(
				new VideoDisplayListener<MBFImage>() {
					@Override
					public void beforeUpdate(MBFImage frame) {
						final FaceDetector<DetectedFace, FImage> fd = new HaarCascadeDetector(40);
						final List<DetectedFace> faces = fd.detectFaces(Transforms.calculateIntensity(frame));

						for (final DetectedFace face : faces) {
							frame.drawShape(face.getBounds(), RGBColour.RED);
						}
					}

					@Override
					public void afterUpdate(VideoDisplay<MBFImage> display) {
					}
				});
	}
}
