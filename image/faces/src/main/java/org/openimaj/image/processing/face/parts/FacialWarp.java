package org.openimaj.image.processing.face.parts;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.face.parts.FacialKeypoint.FacialKeypointType;
import org.openimaj.image.processing.transform.NonLinearWarp;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.util.pair.Pair;

import Jama.Matrix;

public class FacialWarp {
	static String [][] def = {
			{"EYE_LEFT_RIGHT", "EYE_RIGHT_LEFT", "NOSE_MIDDLE"},
			{"EYE_LEFT_LEFT", "EYE_LEFT_RIGHT", "NOSE_LEFT"},
			{"EYE_RIGHT_RIGHT", "EYE_RIGHT_LEFT", "NOSE_RIGHT"},
			{"EYE_LEFT_RIGHT", "NOSE_LEFT", "NOSE_MIDDLE"},
			{"EYE_RIGHT_LEFT", "NOSE_RIGHT", "NOSE_MIDDLE"},
			{"MOUTH_LEFT", "MOUTH_RIGHT", "NOSE_MIDDLE"},
			{"MOUTH_LEFT", "NOSE_LEFT", "NOSE_MIDDLE"},
			{"MOUTH_RIGHT", "NOSE_RIGHT", "NOSE_MIDDLE"},
			{"MOUTH_LEFT", "NOSE_LEFT", "EYE_LEFT_LEFT"},
			{"MOUTH_RIGHT", "NOSE_RIGHT", "EYE_RIGHT_RIGHT"},
			
			{"P0", "EYE_LEFT_LEFT", "EYE_LEFT_RIGHT"},
			{"P1", "EYE_RIGHT_RIGHT", "EYE_RIGHT_LEFT"},
//			{"P0", "EYE_LEFT_RIGHT", "EYE_RIGHT_LEFT", "P1"},
//			{"P3", "MOUTH_LEFT", "MOUTH_RIGHT", "P2"},
			
			{"P0", "EYE_LEFT_LEFT", "MOUTH_LEFT"},
			{"P1", "EYE_RIGHT_RIGHT", "MOUTH_RIGHT"},
//			{"P0", "P3", "MOUTH_LEFT"},			
//			{"P1", "P2", "MOUTH_RIGHT"},
			
			
			{"P0", "EYE_LEFT_RIGHT", "EYE_RIGHT_LEFT"},
			{"P1", "EYE_RIGHT_LEFT", "EYE_LEFT_RIGHT"},
			
			{"P3", "MOUTH_LEFT", "MOUTH_RIGHT"},
			{"P2", "MOUTH_RIGHT", "MOUTH_LEFT"},
			
//			{"P3", "EYE_LEFT_LEFT", "MOUTH_LEFT"},
//			{"P2", "EYE_RIGHT_RIGHT", "MOUTH_RIGHT"},
	};
	
	final static float [][] Pmu = {{25.0347f, 34.1802f, 44.1943f, 53.4623f, 34.1208f, 39.3564f, 44.9156f, 31.1454f, 47.8747f},
		   {34.1580f, 34.1659f, 34.0936f, 33.8063f, 45.4179f, 47.0043f, 45.3628f, 53.0275f, 52.7999f}};

	Point2d P0 = new Point2dImpl(0,0);
	Point2d P1 = new Point2dImpl(80,0);
	Point2d P2 = new Point2dImpl(80,80);
	Point2d P3 = new Point2dImpl(0,80);
	
	public FacialKeypoint[] loadCanonicalPoints() {
		FacialKeypoint[] points = new FacialKeypoint[Pmu[0].length];
		
		for (int i=0; i<points.length; i++) {
			points[i] = new FacialKeypoint(FacialKeypointType.valueOf(i));
			points[i].imagePosition = new Point2dImpl(2*Pmu[0][i]-40, 2*Pmu[1][i]-40);
		}
		
		return points;
	}
	
	public FacialKeypoint[] getActualPoints(FacialKeypoint[] keys, Matrix tf0) {
		FacialKeypoint[] points = new FacialKeypoint[Pmu[0].length];
		
		for (int i=0; i<points.length; i++) {
			
			points[i] = new FacialKeypoint(FacialKeypointType.valueOf(i));
			points[i].imagePosition = new Point2dImpl(FacialKeypoint.getKeypoint(keys, FacialKeypointType.valueOf(i)).imagePosition.transform(tf0));
		}
		return points;
	}
	
	public List<Pair<Shape>> getMesh(FacialKeypoint[] det, FacialKeypoint[] can, String [][] meshDef) {
		List<Pair<Shape>> shapes = new ArrayList<Pair<Shape>>();
		
		for (String [] vertDefs : meshDef) {
			Polygon p1 = new Polygon();
			Polygon p2 = new Polygon();
			
			for (String v : vertDefs) {
				p1.getVertices().add( lookupVertex(v, det) );
				p2.getVertices().add( lookupVertex(v, can) );
			}
			shapes.add(new Pair<Shape>(p1, p2));
		}
		
		return shapes;
	}
	
	
	
	private Point2d lookupVertex(String v, FacialKeypoint[] pts) {
		if (v.equals("P0")) return P0;
		if (v.equals("P1")) return P1;
		if (v.equals("P2")) return P2;
		if (v.equals("P3")) return P3;
		
		return FacialKeypoint.getKeypoint(pts, FacialKeypointType.valueOf(v)).imagePosition;
	}

	
	
	public FImage getWarpedImage(FacialKeypoint[] kpts, FImage patch, Matrix tf0) {
//		FImage image = ImageUtilities.readF(new File("/Volumes/Raid/face_databases/gt_db/s12/01.jpg"));
//		
//		FacePipeline pipe = new FacePipeline();
//		DetectedFace face = pipe.extractFaces(image).get(0);
//		
//		Point2d lefteye = face.getPartDescriptor(FacialKeypointType.EYE_LEFT_LEFT).position;
//		Point2d righteye = face.getPartDescriptor(FacialKeypointType.EYE_RIGHT_RIGHT).position;
//		
//		int eyeDist = 50;
//		int eyePaddingLeftRight = 15;
//		int eyePaddingTop = 20;
//		
//		float dx = righteye.getX() - lefteye.getX();
//		float dy = righteye.getY() - lefteye.getY();
//		
//		float rotation = (float) Math.atan2(dy, dx);
//		float scaling = (float) (eyeDist / Math.sqrt(dx*dx + dy*dy));
//		
//		float tx = lefteye.getX() - eyePaddingLeftRight / scaling;
//		float ty = lefteye.getY() - eyePaddingTop / scaling;
//		
//		Matrix tf = TransformUtilities.scaleMatrix(scaling, scaling).times(TransformUtilities.translateMatrix(-tx, -ty)).times(TransformUtilities.rotationMatrixAboutPoint(-rotation, lefteye.getX(), lefteye.getY()));
//	
//		Matrix tf0 = tf.copy();
//		tf = tf.inverse();
//		
//		FImage J = FacePipeline.pyramidResize(image, tf);
//		FImage patch = FacePipeline.extractPatch(J, tf, 80, 0);
//		
		FacialKeypoint [] can = loadCanonicalPoints();
		FacialKeypoint [] det = getActualPoints(kpts, tf0);
		List<Pair<Shape>> mesh = getMesh(det, can, def);
		
		FImage newpatch = patch.process(new NonLinearWarp<Float, FImage>(mesh));
		
		return newpatch;
	}
	
//	public static void main(String [] args) throws IOException {
//		new FacialWarp().test();
//	}
}
