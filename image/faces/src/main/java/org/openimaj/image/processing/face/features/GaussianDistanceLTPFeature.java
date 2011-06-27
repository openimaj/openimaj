package org.openimaj.image.processing.face.features;

import org.openimaj.image.processing.face.alignment.FaceAligner;
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
	private static final long serialVersionUID = 1L;
	
	public static class Factory implements FacialFeatureFactory<GaussianDistanceLTPFeature> {
		private static final long serialVersionUID = 1L;
		
		private float sigma = 3;
		private FaceAligner aligner;
		
		public Factory(FaceAligner aligner) {
			this.aligner = aligner;
		}
		
		public Factory(FaceAligner aligner, float sigma) {
			this(aligner);
			this.sigma = sigma;
		}
		
		@Override
		public GaussianDistanceLTPFeature createFeature(DetectedFace face, boolean isquery) {
			GaussianDistanceLTPFeature f = new GaussianDistanceLTPFeature(aligner, sigma);
			f.initialise(face, isquery);
			return f;
		}
	}
	
	float sigma = 3;
	
	/**
	 * Construct the GaussianDistanceLTPFeature with the default
	 * sigma of 3 pixels and specified aligner.
	 */
	public GaussianDistanceLTPFeature(FaceAligner aligner) {
		super(aligner);
	}
	
	/**
	 * Construct the GaussianDistanceLTPFeature with the provided
	 * sigma and specified aligner.
	 * @param sigma the variance of the Gaussian weighting
	 */
	public GaussianDistanceLTPFeature(FaceAligner aligner, float sigma) {
		this(aligner);
		this.sigma = sigma;
	}
	

	@Override
	protected float weightDistance(float distance) {
		return (float) Math.exp( -(distance / sigma) * (distance / sigma) / 2);
	}
}
