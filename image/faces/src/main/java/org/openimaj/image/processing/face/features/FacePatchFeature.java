package org.openimaj.image.processing.face.features;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.FloatFV;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.face.alignment.AffineAligner;
import org.openimaj.image.processing.face.parts.KEDetectedFace;
import org.openimaj.image.processing.face.parts.FacialKeypoint;
import org.openimaj.image.processing.face.parts.FacialKeypoint.FacialKeypointType;
import org.openimaj.image.processing.face.parts.FKEFaceDetector;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

import Jama.Matrix;

/**
 * A {@link FacialFeature} that is built by concatenating
 * each of the normalised facial part patches from a detected
 * face. 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class FacePatchFeature implements FacialFeature<FacePatchFeature, KEDetectedFace> {
	public static class Factory implements FacialFeatureFactory<FacePatchFeature, KEDetectedFace> {
		
		FloatFVComparison comp = FloatFVComparison.EUCLIDEAN;
		
		public Factory() {}
		public Factory(FloatFVComparison comp) {
			this.comp = comp;
		}
		
		@Override
		public FacePatchFeature createFeature(KEDetectedFace face, boolean isquery) {
			FacePatchFeature f = new FacePatchFeature(comp);
			f.initialise(face, isquery);
			return f;
		}
	}
	
	public static class DetectedFacePart extends FacialKeypoint {
		float [] featureVector;
		int featureRadius;
		
		public DetectedFacePart(FacialKeypointType type, Point2d position) {
			super(type, position);
		}
		
		public FImage getImage() {
			FImage image = new FImage(2*featureRadius+1,2*featureRadius+1);
			
			for (int i=0, rr=-featureRadius; rr<=featureRadius; rr++) {
				for (int cc=-featureRadius; cc<=featureRadius; cc++) {
					float r2 = rr*rr + cc*cc;
					
					if (r2<=featureRadius*featureRadius) { //inside circle
						float value = featureVector[i++];
						
						image.pixels[rr + featureRadius][cc + featureRadius] = value < -3 ? 0 : value >=3 ? 1 : (3f + value) / 6f;  
					}
				}
			}
			
			return image;
		}
	}
	
	final static int [][] VP = {
		{0}, //	EYE_LEFT_LEFT, 
		{1}, //	EYE_LEFT_RIGHT,
		{2}, //	EYE_RIGHT_LEFT,
		{3}, //	EYE_RIGHT_RIGHT,
		{4}, //	NOSE_LEFT,
		{5}, //	NOSE_MIDDLE,
		{6}, //	NOSE_RIGHT,
		{7}, //	MOUTH_LEFT,
		{8}, //	MOUTH_RIGHT,
		{0, 1}, //	EYE_LEFT_CENTER,
		{2, 3}, //	EYE_RIGHT_CENTER,
		{1, 2}, //	NOSE_BRIDGE,
		{7, 8}}; //	MOUTH_CENTER
	
	protected FloatFV featureVector;
	protected FloatFVComparison comp = FloatFVComparison.EUCLIDEAN;
	
	/** The radius of the descriptor samples about each point */
	protected int radius = 7;
	
	/** The scale of the descriptor samples about each point */
	protected float scl = 1;
	
	protected List<DetectedFacePart> faceParts = new ArrayList<DetectedFacePart>();

	/**
	 * Default constructor. Uses the Euclidean distance for
	 * feature comparison.
	 */
	public FacePatchFeature() {
	}
	
	/**
	 * Construct the FacePatchFeature using the provided 
	 * distance measure for comparison.
	 *
	 * Note that different distance measures to Euclidean 
	 * might reverse the meaning of the score.
	 *  
	 * @param face the face
	 */
	public FacePatchFeature(FloatFVComparison comp) {
		this.comp = comp;
	}

	@Override
	public void initialise(KEDetectedFace face, boolean isQuery) {
		extractFeatures(face);
		this.featureVector = createFeatureVector();
	}

	protected FloatFV createFeatureVector() {
		int length = faceParts.get(0).featureVector.length;
		FloatFV fv = new FloatFV(faceParts.size() * length);
		
		for (int i=0; i<faceParts.size(); i++) {
			System.arraycopy(faceParts.get(i).featureVector, 0, fv.values, i*length, length);
		}
		
		return fv;
	}
	
	@Override
	public double compare(FacePatchFeature feature) {
		return featureVector.compare(feature.featureVector, comp);
	}
	
	protected void extractFeatures(KEDetectedFace face) {
		Matrix T0 = AffineAligner.estimateAffineTransform(face);
		Matrix T = T0.copy();
		FImage J = FKEFaceDetector.pyramidResize(face.getFacePatch(), T);
		FacialKeypoint[] pts = face.getKeypoints();
		faceParts.clear();
		
		float pyrScale = (float) (T0.get(0,2) / T.get(0, 2));
		
		//build a list of the center of each patch wrt image J
		Point2dImpl[] P0 = new Point2dImpl[VP.length];
		for (int j=0; j<P0.length; j++) {
			int [] vp = VP[j];
			int vp0 = vp[0];
			
			P0[j] = new Point2dImpl(0, 0);
			if (vp.length == 1) {
				P0[j].x = pts[vp0].position.x / pyrScale;
				P0[j].y = pts[vp0].position.y / pyrScale;
			} else {
				int vp1 = vp[1];
				P0[j].x = ((pts[vp0].position.x + pts[vp1].position.x) / 2.0f) / pyrScale;
				P0[j].y = ((pts[vp0].position.y + pts[vp1].position.y) / 2.0f) / pyrScale;
			}
		}
		
		//Prebuild transform
		List<Point2dImpl> transformed = new ArrayList<Point2dImpl>();
		List<Pixel> nontransformed = new ArrayList<Pixel>();
		for (int rr=-radius; rr<=radius; rr++) {
			for (int cc=-radius; cc<=radius; cc++) {
				float r2 = rr*rr + cc*cc;
				if (r2<=radius*radius) { //inside circle
					//Note: do transform without the translation!!!
					float px = (float) (cc*scl* T.get(0, 0) + rr*scl*T.get(0, 1));
					float py = (float) (cc*scl* T.get(1, 0) + rr*scl*T.get(1, 1));
					
					transformed.add(new Point2dImpl(px, py));
					nontransformed.add(new Pixel(cc,rr));
				}
			}
		}
		
		for (int j=0; j<VP.length; j++) {
			DetectedFacePart pd = new DetectedFacePart(FacialKeypointType.valueOf(j), new Point2dImpl(P0[j].x * pyrScale, P0[j].y * pyrScale));
			faceParts.add(pd);
			pd.featureVector = new float[transformed.size()];
			
			int n = 0;
			float mean = 0;
			float m2 = 0;
			
			for (int i=0; i<transformed.size(); i++) {
				Point2dImpl XYt = transformed.get(i);
				
				double xt = XYt.x + P0[j].x;
				double yt = XYt.y + P0[j].y;
				float val = J.getPixelInterp(xt, yt);
				
				pd.featureVector[i] = val;
				
				n++;
				float delta = val - mean;
				mean = mean + delta / n;
				m2 = m2 + delta*(val - mean);
			}
			
			float std = (float) Math.sqrt(m2 / (n-1));
			if (std <= 0) std = 1;
			
			for (int i=0; i<transformed.size(); i++) {
				pd.featureVector[i] = (pd.featureVector[i] - mean) / std;
			}
		}
	}
}
