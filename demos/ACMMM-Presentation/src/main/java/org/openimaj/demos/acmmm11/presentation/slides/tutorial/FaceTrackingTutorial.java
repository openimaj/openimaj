package org.openimaj.demos.acmmm11.presentation.slides.tutorial;

import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.video.Video;

public class FaceTrackingTutorial extends TutorialPanel {
	private static final long serialVersionUID = -5279460790389377219L;
	
	private HaarCascadeDetector detector;
	
	public FaceTrackingTutorial(Video<MBFImage> capture, int width, int height){
		super("Face Tracking",capture,width,height);
		
		this.detector = new HaarCascadeDetector(height/3);
	}

	@Override
	public void doTutorial(MBFImage toDraw) {
		List<DetectedFace> faces = this.detector.detectFaces(toDraw.flatten());
		
		for (DetectedFace detectedFace : faces) {
			toDraw.drawShape(detectedFace.getBounds(), RGBColour.RED);
		}
	}	
}
