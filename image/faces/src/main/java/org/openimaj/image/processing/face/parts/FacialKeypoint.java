package org.openimaj.image.processing.face.parts;

import org.openimaj.image.pixel.Pixel;
import org.openimaj.math.geometry.point.Point2dImpl;

import Jama.Matrix;

public class FacialKeypoint {
	public static enum FacialKeypointType {
		EYE_LEFT_LEFT,
		EYE_LEFT_RIGHT,
		EYE_RIGHT_LEFT,
		EYE_RIGHT_RIGHT,
		NOSE_LEFT,
		NOSE_MIDDLE,
		NOSE_RIGHT,
		MOUTH_LEFT,
		MOUTH_RIGHT,
		EYE_LEFT_CENTER,
		EYE_RIGHT_CENTER,
		NOSE_BRIDGE,
		MOUTH_CENTER
		;
		
		public static FacialKeypointType valueOf(int ordinal) {
			return FacialKeypointType.values()[ordinal]; 
		}
	}

	public FacialKeypointType type;
	public Pixel canonicalPosition;
	public Point2dImpl imagePosition;
	
	public FacialKeypoint(FacialKeypointType type) {
		this.type = type;
		canonicalPosition = new Pixel(0, 0);
		imagePosition = new Point2dImpl(0, 0);
	}
	
	protected void updateImagePosition(Matrix transform) {
		imagePosition.x = (float) (canonicalPosition.x*transform.get(0, 0) + canonicalPosition.y*transform.get(0, 1) + transform.get(0, 2));
		imagePosition.y = (float) (canonicalPosition.x*transform.get(1, 0) + canonicalPosition.y*transform.get(1, 1) + transform.get(1, 2));
	}
	
	protected static void updateImagePosition(FacialKeypoint[] kpts, Matrix transform) {
		for (FacialKeypoint k : kpts) k.updateImagePosition(transform);
	}
}
