package org.openimaj.image.saliency;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processor.ImageProcessor;

/**
 * Interface for classes capable of processing images (as an image processor)
 * to generate saliency maps.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <I> type of image
 */
public interface SaliencyMapGenerator<I extends Image<?,I>> extends ImageProcessor<I> {
	/**
	 * Get the generated saliency map
	 * @return the saliency map
	 */
	public FImage getSaliencyMap();
}
