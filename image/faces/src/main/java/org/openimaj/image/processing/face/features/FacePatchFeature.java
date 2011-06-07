package org.openimaj.image.processing.face.features;

import java.util.List;

import org.openimaj.feature.FloatFV;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.image.processing.face.parts.DetectedFace;
import org.openimaj.image.processing.face.parts.DetectedFace.DetectedFacePart;

/**
 * A {@link FacialFeature} that is built by concatenating
 * each of the normalised facial part patches from a detected
 * face. 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class FacePatchFeature extends FacialFeature<FacePatchFeature> {
	public static class Factory implements FacialFeatureFactory<FacePatchFeature> {
		FloatFVComparison comp = FloatFVComparison.EUCLIDEAN;
		
		public Factory() {}
		public Factory(FloatFVComparison comp) {
			this.comp = comp;
		}
		
		@Override
		public FacePatchFeature createFeature(DetectedFace face, boolean isquery) {
			return new FacePatchFeature(face, comp);
		}
	}
	
	protected FloatFV featureVector;
	protected FloatFVComparison comp = FloatFVComparison.EUCLIDEAN;

	/**
	 * Construct the FacePatchFeature for the given face,
	 * and use the default Euclidean distance measure for 
	 * comparison. 
	 * @param face the face
	 */
	public FacePatchFeature(DetectedFace face) {
		super(face);
	}
	
	/**
	 * Construct the FacePatchFeature for the given face,
	 * and use the provided distance measure for comparison.
	 *
	 * Note that different distance measures to Euclidean 
	 * might reverse the meaning of the score.
	 *  
	 * @param face the face
	 */
	public FacePatchFeature(DetectedFace face, FloatFVComparison comp) {
		super(face);
		this.comp = comp;
	}

	@Override
	protected void initialise(DetectedFace face) {
		this.featureVector = getFeatureVector(face.faceParts);
	}

	protected FloatFV getFeatureVector(List<DetectedFacePart> faceParts) {
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
}
