package org.openimaj.feature.keypoints.face;

import org.openimaj.image.processing.face.parts.DetectedFaceExtractor;
import org.openimaj.image.processing.face.parts.FacialKeypoint.FacialKeypointType;
import org.openimaj.math.geometry.point.Point2d;

public class FacialPartRatio {
	public static void main(String[] args){
		Point2d eyeLL = DetectedFaceExtractor.facePartPosition(FacialKeypointType.EYE_LEFT_LEFT).get(0);
		Point2d eyeRL = DetectedFaceExtractor.facePartPosition(FacialKeypointType.EYE_RIGHT_LEFT).get(0);
		Point2d eyeLR = DetectedFaceExtractor.facePartPosition(FacialKeypointType.EYE_LEFT_RIGHT).get(0);
		Point2d eyeRR = DetectedFaceExtractor.facePartPosition(FacialKeypointType.EYE_RIGHT_RIGHT).get(0);
		
		Point2d noseL = DetectedFaceExtractor.facePartPosition(FacialKeypointType.NOSE_LEFT).get(0);
		Point2d noseM = DetectedFaceExtractor.facePartPosition(FacialKeypointType.NOSE_MIDDLE).get(0);
		Point2d noseR = DetectedFaceExtractor.facePartPosition(FacialKeypointType.NOSE_RIGHT).get(0);
		
		double avgEyeline = (eyeLL.getY() + eyeLR.getY() + eyeRL.getY() + eyeRR.getY())/4.0;
		double noseBottom = (noseL.getY() + noseR.getY() )/2.0;
		double noseWidth = noseR.getX() - noseL.getX();
		double noseHeight = noseBottom - avgEyeline;
		double eyeWidth = eyeRL.getX() - eyeLR.getX();
		
		System.out.println("Nose Height: " + noseHeight);
		System.out.println("Nose Width: " + noseWidth);
		System.out.println("Eye Width: " + eyeWidth);
		
		
	}
}
