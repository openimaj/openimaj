package org.openimaj.demos.acmmm11.presentation.slides.tutorial;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.keypoints.FacialKeypoint;
import org.openimaj.image.processing.face.keypoints.KEDetectedFace;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.Video;

public class FaceKETrackingTutorial extends TutorialPanel {
private static final long serialVersionUID = -5279460790389377219L;
	
	private FaceDetector<KEDetectedFace,FImage> detector;
	
	public FaceKETrackingTutorial(Video<MBFImage> capture, int width, int height){
		super("Face Finding", capture, width, height);
		
		this.detector = new FKEFaceDetector( new HaarCascadeDetector(height/3));
	}

	@Override
	public void doTutorial(MBFImage toDraw) {
		List<KEDetectedFace> faces = this.detector.detectFaces(toDraw.flatten());
		
		for (KEDetectedFace detectedFace : faces) {
			Rectangle b = detectedFace.getBounds();
			Point2dImpl bp = new Point2dImpl(b.x,b.y);
			toDraw.drawShape(b, RGBColour.RED);
			FacialKeypoint[] kpts = detectedFace.getKeypoints();
			List<Point2d> fpts = new ArrayList<Point2d>();
			for(FacialKeypoint kpt : kpts){
				Point2dImpl p = kpt.position;
				p.translate(bp);
				fpts.add(p);
			}
			toDraw.drawPoints(fpts, RGBColour.GREEN, 3);
		}
	}
}
