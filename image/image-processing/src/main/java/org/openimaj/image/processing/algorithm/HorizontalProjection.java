package org.openimaj.image.processing.algorithm;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processor.ImageProcessor;

/**
 * Project an image onto the x-axis.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class HorizontalProjection implements ImageProcessor<FImage> {
	float [] projection;
	
	@Override
	public void processImage(FImage image, Image<?, ?>... otherimages) {
		projection = project(image);
	}

	/**
	 * Project an image onto the x-axis.
	 * 
	 * @param image the image
	 * @return the projection
	 */
	public static float[] project(FImage image) {
		float [] projection = new float[image.width];
		
		for (int y=0; y<image.height; y++) {
			for (int x=0; x<image.width; x++) {
				projection[x] += image.pixels[y][x];
			}
		}
		
		return projection;
	}

	public float[] getProjection() {
		return projection;
	}
}
