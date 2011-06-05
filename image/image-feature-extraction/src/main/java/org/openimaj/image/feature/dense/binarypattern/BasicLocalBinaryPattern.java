package org.openimaj.image.feature.dense.binarypattern;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.ImageProcessor;

/**
 * Implementation of the original 3x3 form of a local binary pattern:
 * 
 * Ojala, T., Pietikainen, M., Harwood, D.: A comparative study of texture 
 * measures with classification based on feature distributions. 
 * Pattern Recognition 29 (1996)
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class BasicLocalBinaryPattern implements ImageProcessor<FImage> {
	protected int[][] pattern = null;
	
	/**
	 * Get the pattern created during the last call to
	 * {@link #processImage(FImage, Image[])}.
	 * 
	 * @return the pattern
	 */
	public int[][] getPattern() {
		return pattern;
	}
	
	/**
	 * Calculate the LBP for every pixel in the image. The returned
	 * array of LBP codes hase the same dimensions as the image. 
	 * 
	 * Samples taken from outside the image bounds are assumed to have 
	 * the value 0.
	 * 
	 * @param image the image
	 * @return a 2d-array of the LBP codes for every pixel
	 */
	public static int[][] calculateLBP(FImage image) {
		int [][] pattern = new int[image.height][image.width];
		
		for (int y=0; y<image.height; y++) {
			for (int x=0; x<image.width; x++) {
				pattern[y][x] = calculateLBP(image, x, y);
			}
		}
		
		return pattern;
	}
	
	/**
	 * Calculate the basic LBP for a single point. The
	 * point must be within the image.
	 * 
	 * @param image the image
	 * @param x the x-coordinate of the point
	 * @param y the y-coordinate of the point
	 * @return the LBP code
	 */
	public static int calculateLBP(FImage image, int x, int y) {
		float thresh = image.pixels[y][x];
		int i = 0;
		int pattern = 0;
		
		for (int yy=-1; yy<2; yy++) {
			for (int xx=-1; xx<2; xx++) {
				if (xx == 0 && yy == 0)
					continue;
				
				int xxx = x + xx;
				int yyy = y + yy;
				float pix = 0;
				
				if (xxx >= 0 && xxx < image.width && yyy >= 0 && yyy < image.height) {
					pix = image.pixels[yyy][xxx];
				}
				
				if (pix >= thresh) {
					pattern += Math.pow(2, i);
				}
				
				i++;
			}
		}
		
		return pattern;
	}
	
	/**
	 * Calculate the basic LBP for a single point. The
	 * point must be within the image.
	 * 
	 * @param image the image
	 * @param point the point
	 * @return the LBP code
	 */
	public static int calculateLBP(FImage image, Pixel point) {
		return calculateLBP(image, point.x, point.y);
	}

	@Override
	public void processImage(FImage image, Image<?, ?>... otherimages) {
		pattern = calculateLBP(image);
	}
}
