package org.openimaj.image.processing.face.alignment;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.parts.DetectedFace;
import org.openimaj.image.processing.face.parts.FacialKeypoint;
import org.openimaj.image.processing.face.parts.FrontalFaceEngine;
import org.openimaj.image.processing.face.parts.FacialKeypoint.FacialKeypointType;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

public class RotateScaleAligner implements FaceAligner {
	//Define the geometry
	private int eyeDist = 56;
	private int eyePaddingLeftRight = 12;
	private int eyePaddingTop = 20;

	private static final FImage DEFAULT_MASK = loadDefaultMask();
	
	private FImage mask = DEFAULT_MASK;
	
	public RotateScaleAligner() {};
	
	public RotateScaleAligner(FImage mask) {
		this.mask = mask;
	}
	
	@Override
	public FImage align(DetectedFace descriptor) {
		FacialKeypoint lefteye = descriptor.getKeypoint(FacialKeypointType.EYE_LEFT_LEFT);
		FacialKeypoint righteye = descriptor.getKeypoint(FacialKeypointType.EYE_RIGHT_RIGHT);
		
		float dx = righteye.position.x - lefteye.position.x;
		float dy = righteye.position.y - lefteye.position.y;
		
		float rotation = (float) Math.atan2(dy, dx);
		float scaling = (float) (eyeDist / Math.sqrt(dx*dx + dy*dy));
		
		float tx = lefteye.position.x - eyePaddingLeftRight / scaling;
		float ty = lefteye.position.y - eyePaddingTop / scaling;
		
		Matrix tf0 = TransformUtilities.scaleMatrix(scaling, scaling).times(TransformUtilities.translateMatrix(-tx, -ty)).times(TransformUtilities.rotationMatrixAboutPoint(-rotation, lefteye.position.x, lefteye.position.y));
		Matrix tf = tf0.inverse();
		
		FImage J = FrontalFaceEngine.pyramidResize(descriptor.getFacePatch(), tf);
		return FrontalFaceEngine.extractPatch(J, tf, 2*eyePaddingLeftRight + eyeDist, 0);
	}
	
	private static FImage loadDefaultMask() {
		try {
			return ImageUtilities.readF(FaceAligner.class.getResourceAsStream("affineMask.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public FImage getMask() {
		return mask;
	}

	public static void main(String [] args) throws Exception {
		FImage image1 = ImageUtilities.readF(new File("/Volumes/Raid/face_databases/faces/image_0001.jpg"));
		List<DetectedFace> faces = new FrontalFaceEngine().extractFaces(image1);
		
		RotateScaleAligner warp = new RotateScaleAligner();
		DisplayUtilities.display(warp.align(faces.get(0)));
		DisplayUtilities.display(warp.getMask());
	}
}
