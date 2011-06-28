package org.openimaj.image.processing.face.feature.ltp;

public interface LTPWeighting {
	/**
	 * Determine the weighting scheme for the distances produced
	 * by the EuclideanDistanceTransform.
	 * @param distance the unweighted distance in pixels
	 * @return the weighted distance
	 */
	public abstract float weightDistance(float distance);
}
