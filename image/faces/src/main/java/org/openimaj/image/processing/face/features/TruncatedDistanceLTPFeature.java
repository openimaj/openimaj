package org.openimaj.image.processing.face.features;

import org.openimaj.image.processing.face.parts.DetectedFace;

/**
 * LTP based feature using a truncated Euclidean distance transform
 * to estimate the distances within each slice.
 * 
 * Based on: 
 * "Enhanced Local Texture Feature Sets for Face Recognition 
 * Under Difficult Lighting Conditions" by Xiaoyang Tan and 
 * Bill Triggs.
 *
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class TruncatedDistanceLTPFeature extends AbstractLTPFeature<TruncatedDistanceLTPFeature> {
	public static class Factory implements FacialFeatureFactory<TruncatedDistanceLTPFeature> {
		float threshold = 6;
		boolean affineMode = false;
		
		public Factory() {}
		public Factory(float sigma) {
			this.threshold = sigma;
		}
		public Factory(float sigma, boolean affineMode) {
			this.threshold = sigma;
			this.affineMode = affineMode;
		}
		
		@Override
		public TruncatedDistanceLTPFeature createFeature(DetectedFace face, boolean isquery) {
			return new TruncatedDistanceLTPFeature(face, isquery, threshold, affineMode);
		}
	}
	
	float threshold = 6;
	
	/**
	 * Construct the TruncatedDistanceLTPFeature with the default
	 * threshold of 6 pixels for the given face. Defaults to 
	 * non-affine normalised faces (i.e. just the eye rotation and
	 * position is optimised).
	 * @param face the face
	 */
	public TruncatedDistanceLTPFeature(DetectedFace face, boolean isquery) {
		this(face, isquery, false);
	}
	
	/**
	 * Construct the TruncatedDistanceLTPFeature with the provided
	 * threshold for the given face. Defaults to non-affine normalised
	 * faces (i.e. just the eye rotation and position is optimised).
	 * @param face the face
	 * @param threshold the threshold
	 */
	public TruncatedDistanceLTPFeature(DetectedFace face, boolean isquery, float threshold) {
		this(face, isquery, false);
		this.threshold = threshold;
	}
	
	/**
	 * Construct the TruncatedDistanceLTPFeature with the default
	 * threshold of 6 pixels for the given face. The affineMode
	 * parameter can be used to enable the feature on affine normalised
	 * faces.
	 * @param face the face
	 * @param affineMode set to true to enable usage on affine normalised faces
	 */
	public TruncatedDistanceLTPFeature(DetectedFace face, boolean isquery, boolean affineMode) {
		super(face, isquery, affineMode);
	}

	/**
	 * Construct the TruncatedDistanceLTPFeature with the provided
	 * threshold for the given face. The affineMode parameter can be 
	 * used to enable the feature on affine normalised faces.
	 * @param face the face
	 * @param threshold the threshold
	 * @param affineMode set to true to enable usage on affine normalised faces
	 */
	public TruncatedDistanceLTPFeature(DetectedFace face, boolean isquery, float threshold, boolean affineMode) {
		this(face, isquery, affineMode);
		this.threshold = threshold;
	}

	@Override
	protected float weightDistance(float distance) {
		return Math.min(distance, threshold);
	}
}
