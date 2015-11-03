package org.openimaj.image.feature.local.interest;

/**
 * A multiscale detector of interest points.
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T>
 *            The type of {@link InterestPointData}
 *
 */
public interface MultiscaleInterestPointDetector<T extends InterestPointData> extends InterestPointDetector<T> {
	/**
	 * Set the detection scale for the detector
	 *
	 * @param detectionScaleVariance
	 *            the variance of the Gaussian
	 */
	public void setDetectionScale(float detectionScaleVariance);
}
