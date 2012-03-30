package org.openimaj.demos.hardware;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.processor.PixelProcessor;

public class DepthCorrectionProcessor implements ImageProcessor<FImage> {
	
	private final static double KINECT_HORIZONTAL_FOV = 57d * (Math.PI/180);
//	private final static double KINECT_HORIZONTAL_FOV = 360d * (Math.PI/180);
	
	@Override
	public void processImage(FImage image) {
		int centerX = image.width/2;
		int centerY = image.height/2;
		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				double theta = (KINECT_HORIZONTAL_FOV/2) * (Math.abs(centerX - x)/(image.width/2.0));
				image.pixels[y][x] *= Math.cos(theta);
			}
		}
	}


}
