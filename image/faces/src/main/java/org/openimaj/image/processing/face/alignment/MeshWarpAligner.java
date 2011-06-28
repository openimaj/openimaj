package org.openimaj.image.processing.face.alignment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.keypoints.FacialKeypoint;
import org.openimaj.image.processing.face.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.keypoints.FacialKeypoint.FacialKeypointType;
import org.openimaj.image.processing.transform.NonLinearWarp;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.pair.Pair;

import Jama.Matrix;

/**
 * A MeshWarpAligner aligns facial images using a non-linear warping
 * such that all detected facial keypoints are moved to their canonical
 * coordinates. The warping is accomplished by defining a mesh of
 * triangles and quadrilaterals over the facial keypoints and using
 * bi-linear interpolation to get corrected pixel values.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class MeshWarpAligner implements FaceAligner<KEDetectedFace> {
	//Define the default mesh
	private static final String [][] DEFAULT_MESH_DEFINITION = {
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
	
	//Define the outer edges
	private static final Point2d P0 = new Point2dImpl(0,0);
	private static final Point2d P1 = new Point2dImpl(80,0);
	private static final Point2d P2 = new Point2dImpl(80,80);
	private static final Point2d P3 = new Point2dImpl(0,80);
	
	//Define the canonical point positions
	private static FacialKeypoint[] canonical = loadCanonicalPoints();
	
	//Define the mesh
	String [][] meshDefinition = DEFAULT_MESH_DEFINITION;

	FImage mask;
	
	/**
	 * Default constructor 
	 */
	public MeshWarpAligner() {
		this(DEFAULT_MESH_DEFINITION);
	}
	
	/**
	 * Construct with the given mesh definition
	 */
	public MeshWarpAligner(String [][] meshDefinition) {
		this.meshDefinition = meshDefinition;
		
		List<Pair<Shape>> mesh = createMesh(canonical);
		
		//build mask by mapping the canonical coords to themselves on a white image
		mask = new FImage((int)P2.getX(), (int)P2.getY());
		mask.fill(1f);
		mask = mask.processInline(new NonLinearWarp<Float, FImage>(mesh));
	}
	
	private static FacialKeypoint[] loadCanonicalPoints() {
		FacialKeypoint[] points = new FacialKeypoint[AffineAligner.Pmu[0].length];
		
		for (int i=0; i<points.length; i++) {
			points[i] = new FacialKeypoint(FacialKeypointType.valueOf(i));
			points[i].position = new Point2dImpl(2*AffineAligner.Pmu[0][i]-40, 2*AffineAligner.Pmu[1][i]-40);
		}
		
		return points;
	}
	
	
	protected FacialKeypoint[] getActualPoints(FacialKeypoint[] keys, Matrix tf0) {
		FacialKeypoint[] points = new FacialKeypoint[AffineAligner.Pmu[0].length];
		
		for (int i=0; i<points.length; i++) {
			points[i] = new FacialKeypoint(FacialKeypointType.valueOf(i));
			points[i].position = new Point2dImpl(FacialKeypoint.getKeypoint(keys, FacialKeypointType.valueOf(i)).position.transform(tf0));
		}
		
		return points;
	}
	
	protected List<Pair<Shape>> createMesh(FacialKeypoint[] det) {
		List<Pair<Shape>> shapes = new ArrayList<Pair<Shape>>();
		
		for (String [] vertDefs : meshDefinition) {
			Polygon p1 = new Polygon();
			Polygon p2 = new Polygon();
			
			for (String v : vertDefs) {
				p1.getVertices().add( lookupVertex(v, det) );
				p2.getVertices().add( lookupVertex(v, canonical) );
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
		
		return FacialKeypoint.getKeypoint(pts, FacialKeypointType.valueOf(v)).position;
	}
	
	@Override
	public FImage align(KEDetectedFace descriptor) {
		float scalingX = P2.getX() / descriptor.getFacePatch().width;
		float scalingY = P2.getY() / descriptor.getFacePatch().height;
		Matrix tf0 = TransformUtilities.scaleMatrix(scalingX, scalingY);
		Matrix tf = tf0.inverse();
		
		FImage J = FKEFaceDetector.pyramidResize(descriptor.getFacePatch(), tf);
		FImage smallpatch = FKEFaceDetector.extractPatch(J, tf, 80, 0);
		
		return getWarpedImage(descriptor.getKeypoints(), smallpatch, tf0);
	}
	
	protected FImage getWarpedImage(FacialKeypoint[] kpts, FImage patch, Matrix tf0) {
		FacialKeypoint [] det = getActualPoints(kpts, tf0);
		List<Pair<Shape>> mesh = createMesh(det);
		
		FImage newpatch = patch.process(new NonLinearWarp<Float, FImage>(mesh));
		
		return newpatch;
	}

	@Override
	public FImage getMask() {
		return mask;
	}	

	public static void main(String [] args) throws Exception {
		FImage image1 = ImageUtilities.readF(new File("/Volumes/Raid/face_databases/faces/image_0001.jpg"));
		List<KEDetectedFace> faces = new FKEFaceDetector().detectFaces(image1);
		
		MeshWarpAligner warp = new MeshWarpAligner();
		DisplayUtilities.display(warp.align(faces.get(0)));
		DisplayUtilities.display(warp.getMask());
	}
}
