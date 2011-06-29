package org.openimaj.image.processing.face.alignment;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.keypoints.FacialKeypoint;
import org.openimaj.image.processing.face.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.keypoints.FacialKeypoint.FacialKeypointType;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

public class RotateScaleAligner implements FaceAligner<KEDetectedFace> {
	private static final FImage DEFAULT_MASK = loadDefaultMask();
	
	//Define the geometry
	private int eyeDist = 56;
	private int eyePaddingLeftRight = 12;
	private int eyePaddingTop = 20;	
	
	private FImage mask = DEFAULT_MASK;
	
	public RotateScaleAligner() {};
	
	public RotateScaleAligner(FImage mask) {
		this.mask = mask;
	}
	
	@Override
	public FImage align(KEDetectedFace descriptor) {
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
		
		FImage J = FKEFaceDetector.pyramidResize(descriptor.getFacePatch(), tf);
		return FKEFaceDetector.extractPatch(J, tf, 2*eyePaddingLeftRight + eyeDist, 0);
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
		List<KEDetectedFace> faces = new FKEFaceDetector().detectFaces(image1);
		
		RotateScaleAligner warp = new RotateScaleAligner();
		DisplayUtilities.display(warp.align(faces.get(0)));
		DisplayUtilities.display(warp.getMask());
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		eyeDist = in.readInt();
		eyePaddingLeftRight = in.readInt();
		eyePaddingTop = in.readInt();
		
		mask = ImageUtilities.readF(in);
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(eyeDist);
		out.writeInt(eyePaddingLeftRight);
		out.writeInt(eyePaddingTop);
		
		ImageUtilities.write(mask, "png", out);
	}
}
