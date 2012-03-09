package org.openimaj.image.pixel.statistics;

import org.openimaj.image.Image;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;

/**
 * Interface for classes capable of building "models"
 * of pixels along a line.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 * 
 * @param <I> Concrete type of {@link Image}
 */
public interface PixelProfileModel<I extends Image<?,I>> {
	/**
	 * Update the model with a new sample.
	 * @param image the image to extract the sample from
	 * @param line the line across with to sample
	 */
	public void updateModel(I image, Line2d line);
	
	/**
	 * Extract numSamples samples from the line in the image and
	 * then compare this model at each overlapping position starting
	 * from the first sample at the beginning of the line.
	 * 
	 * numSamples must be bigger than the number of samples used to
	 * construct the model. In addition, callers are responsible for
	 * ensuring the sampling rate between the new samples and the model
	 * is equal.
	 * 
	 * The point on the line corresponding to the smallest Mahalanobis 
	 * distance is returned.
	 * 
	 * @param image the image to sample
	 * @param line the line to sample along
	 * @param numSamples the number of samples to make
	 * @return the "best" position on the line
	 */
	public Point2d computeNewBest(I image, Line2d line, int numSamples);
	
	/**
	 * Compute the cost of a vector of samples extracted 
	 * along a line in the given image to the internal model. 
	 * 
	 * @param image the image to sample 
	 * @param line the line to sample along
	 * @return the computed cost
	 */
	public float computeCost(I image, Line2d line);
	
	/**
	 * Compute the distance between the centre of the given
	 * line and the given point, normalised as a function of
	 * the length of the sampling line.
	 * 
	 * @param image the image to sample
	 * @param line the line to sample along
	 * @param numSamples the number of samples to make
	 * @param pt the point to compare
	 * @return the normalised distance (0 means same point; 1 means on end of line)
	 */
	public float computeMovementDistance(I image, Line2d line, int numSamples, Point2d pt);
}
