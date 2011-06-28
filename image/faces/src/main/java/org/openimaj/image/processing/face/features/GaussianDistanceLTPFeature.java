package org.openimaj.image.processing.face.features;

import org.openimaj.image.processing.face.alignment.FaceAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;

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
public class GaussianDistanceLTPFeature<Q extends DetectedFace> extends AbstractLTPFeature<GaussianDistanceLTPFeature<Q>, Q> {
	private static final long serialVersionUID = 1L;
	
	public static class Factory<Q extends DetectedFace> implements FacialFeatureFactory<GaussianDistanceLTPFeature<Q>, Q> {
		private static final long serialVersionUID = 1L;
		
		private float sigma = 3;
		private FaceAligner<Q> aligner;
		
		public Factory(FaceAligner<Q> aligner) {
			this.aligner = aligner;
		}
		
		public Factory(FaceAligner<Q> aligner, float sigma) {
			this(aligner);
			this.sigma = sigma;
		}
		
		@Override
		public GaussianDistanceLTPFeature<Q> createFeature(Q face, boolean isquery) {
			GaussianDistanceLTPFeature<Q> f = new GaussianDistanceLTPFeature<Q>(aligner, sigma);
			f.initialise(face, isquery);
			return f;
		}
	}
	
	float sigma = 3;
	
	/**
	 * Construct the GaussianDistanceLTPFeature with the default
	 * sigma of 3 pixels and specified aligner.
	 */
	public GaussianDistanceLTPFeature(FaceAligner<Q> aligner) {
		super(aligner);
	}
	
	/**
	 * Construct the GaussianDistanceLTPFeature with the provided
	 * sigma and specified aligner.
	 * @param sigma the variance of the Gaussian weighting
	 */
	public GaussianDistanceLTPFeature(FaceAligner<Q> aligner, float sigma) {
		this(aligner);
		this.sigma = sigma;
	}
	

	@Override
	protected float weightDistance(float distance) {
		return (float) Math.exp( -(distance / sigma) * (distance / sigma) / 2);
	}
}
