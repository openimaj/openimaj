package org.openimaj.image.processing.face.features;

import org.openimaj.image.processing.face.alignment.FaceAligner;
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
public class ReversedTruncatedDistanceLTPFeature extends AbstractReversedLTPFeature<ReversedTruncatedDistanceLTPFeature> {
	private static final long serialVersionUID = 1L;
	
	public static class Factory implements FacialFeatureFactory<ReversedTruncatedDistanceLTPFeature> {
		private static final long serialVersionUID = 1L;
		
		float threshold = 6;
		FaceAligner aligner;
		
		public Factory(FaceAligner aligner, float threshold) {
			this.aligner = aligner;
			this.threshold = threshold;
		}
		public Factory(FaceAligner aligner) {
			this.aligner = aligner;
		}
		
		@Override
		public ReversedTruncatedDistanceLTPFeature createFeature(DetectedFace face, boolean isquery) {
			ReversedTruncatedDistanceLTPFeature f = new ReversedTruncatedDistanceLTPFeature(aligner, threshold);
			f.initialise(face, isquery);
			return f;
		}
	}
	
	float threshold = 6;
	
	/**
	 * Construct the TruncatedDistanceLTPFeature with the default
	 * threshold of 6 pixels and given aligner.
	 */
	public ReversedTruncatedDistanceLTPFeature(FaceAligner aligner) {
		super(aligner);
	}

	/**
	 * Construct the TruncatedDistanceLTPFeature with the provided
	 * threshold and aligner.
	 * @param threshold the threshold
	 */
	public ReversedTruncatedDistanceLTPFeature(FaceAligner aligner, float threshold) {
		this(aligner);
		this.threshold = threshold;
	}

	@Override
	protected float weightDistance(float distance) {
		return Math.min(distance, threshold);
	}
}
