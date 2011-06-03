package org.openimaj.image.processing.algorithm;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processor.ImageProcessor;

/**
 * Implementation of a difference of Gaussian filter.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class DifferenceOfGaussian implements ImageProcessor<FImage> {
	FGaussianConvolve filter1;
	FGaussianConvolve filter2;
	
	/**
	 * Construct a difference of Gaussian with the default sigmas
	 * of 1 and 2.
	 */
	public DifferenceOfGaussian() {
		this(1, 2);
	}
	
	/**
	 * Construct a difference of Gaussian with the specified 
	 * sigmas.
	 * @param sigma1 first (smaller sigma)
	 * @param sigma2 second sigma
	 */
	public DifferenceOfGaussian(float sigma1, float sigma2) {
		filter1 = new FGaussianConvolve(sigma1);
		filter2 = new FGaussianConvolve(sigma2);
	}
	
	@Override
	public void processImage(FImage image, Image<?, ?>... otherimages) {
		FImage blur1 = image.process(filter1);
		FImage blur2 = image.process(filter2);
		
		image.internalAssign(blur1.subtractInline(blur2));
	}
}
