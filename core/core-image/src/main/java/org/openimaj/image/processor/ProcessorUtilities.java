package org.openimaj.image.processor;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;

/**
 * Utility functions for dealing with {@link Processor}s.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ProcessorUtilities {
	/**
	 * Wrap a {@link SinglebandImageProcessor} for {@link FImage}s in a
	 * {@link ImageProcessor} for {@link MBFImage}s.
	 * 
	 * @param proc
	 *            the processor to wrap
	 * @return the wrapped processor
	 */
	public static ImageProcessor<MBFImage> wrap(final SinglebandImageProcessor<Float, FImage> proc)
	{
		return new ImageProcessor<MBFImage>() {
			@Override
			public void processImage(MBFImage image) {
				image.processInplace(proc);
			}
		};
	}
}
