package org.openimaj.image.processing.algorithm;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processor.ImageProcessor;

/**
 * Project an image onto the y-axis
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class VerticalProjection implements ImageProcessor<FImage> {
	float [] projection;
	
	@Override
	public void processImage(FImage image, Image<?, ?>... otherimages) {
		projection = project(image);
	}

	public static float[] project(FImage image) {
		float [] projection = new float[image.height];
		
		for (int y=0; y<image.height; y++) {
			for (int x=0; x<image.width; x++) {
				projection[y] += image.pixels[y][x];
			}
		}

		return projection;
	}

	public float[] getProjection() {
		return projection;
	}
}
