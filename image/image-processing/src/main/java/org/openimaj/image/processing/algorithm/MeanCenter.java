package org.openimaj.image.processing.algorithm;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.ImageProcessor;

/**
 * An {@link ImageProcessor} that computes the mean of the image's pixels
 * and subtracts the mean from all pixels.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class MeanCenter implements ImageProcessor<FImage> {

	@Override
	public void processImage(FImage image) {
		final int width = image.width;
		final int height = image.height;
		final float[][] data = image.pixels;
		
		float accum = 0;
		
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				accum += data[y][x];
			}
		}
		
		float mean = accum / (float)(width * height);
		
		image.subtractInline(mean);
	}
}
