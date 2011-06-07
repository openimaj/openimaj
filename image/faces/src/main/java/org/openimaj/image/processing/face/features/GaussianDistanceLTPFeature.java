package org.openimaj.image.processing.face.features;

import org.openimaj.image.processing.face.parts.DetectedFace;

/**
 * LTP based feature using a Gaussian weighted Euclidean distance transform
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
public class GaussianDistanceLTPFeature extends AbstractLTPFeature<GaussianDistanceLTPFeature> {
	public static class Factory implements FacialFeatureFactory<GaussianDistanceLTPFeature> {
		float sigma = 3;
		boolean affineMode = false;
		
		public Factory() {}
		public Factory(float sigma) {
			this.sigma = sigma;
		}
		public Factory(float sigma, boolean affineMode) {
			this.sigma = sigma;
			this.affineMode = affineMode;
		}
		
		@Override
		public GaussianDistanceLTPFeature createFeature(DetectedFace face, boolean isquery) {
			GaussianDistanceLTPFeature f = new GaussianDistanceLTPFeature(isquery, sigma, affineMode);
			f.initialise(face);
			return f;
		}
	}
	
	float sigma = 3;
	
	/**
	 * Construct the GaussianDistanceLTPFeature with the default
	 * sigma of 3 pixels. Defaults to 
	 * non-affine normalised faces (i.e. just the eye rotation and
	 * position is optimised).
	 */
	public GaussianDistanceLTPFeature(boolean isquery) {
		this(isquery, false);
	}
	
	/**
	 * Construct the GaussianDistanceLTPFeature with the provided
	 * sigma. Defaults to non-affine normalised
	 * faces (i.e. just the eye rotation and position is optimised).
	 * @param sigma the variance of the Gaussian weighting
	 */
	public GaussianDistanceLTPFeature(boolean isquery, float sigma) {
		this(isquery, false);
		this.sigma = sigma;
	}
	
	/**
	 * Construct the GaussianDistanceLTPFeature with the default
	 * sigma of 3 pixels. The affineMode
	 * parameter can be used to enable the feature on affine normalised
	 * faces.
	 * @param affineMode set to true to enable usage on affine normalised faces
	 */
	public GaussianDistanceLTPFeature(boolean isquery, boolean affineMode) {
		super(isquery, affineMode);
	}

	/**
	 * Construct the GaussianDistanceLTPFeature with the provided
	 * sigma. The affineMode parameter can be 
	 * used to enable the feature on affine normalised faces.
	 * @param sigma the variance of the Gaussian weighting 
	 * @param affineMode set to true to enable usage on affine normalised faces
	 */
	public GaussianDistanceLTPFeature(boolean isquery, float sigma, boolean affineMode) {
		this(isquery, affineMode);
		this.sigma = sigma;
	}

	@Override
	protected float weightDistance(float distance) {
		return (float) Math.exp( -(distance / sigma) * (distance / sigma) / 2);
	}
}
