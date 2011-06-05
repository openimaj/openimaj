package org.openimaj.image.feature.dense.binarypattern;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.ImageProcessor;

/**
 * Implementation of an extended local binary pattern which has a
 * variable number of samples taken from a variable sized circle
 * about a point:
 * 
 * Ojala, T., Pietikainen, M., Maenpaa, T.: Multiresolution 
 * gray-scale and rotation invarianat texture classification with 
 * local binary patterns. IEEE TPAMI 24(7), 971Ð987 (2002)
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class ExtendedLocalBinaryPattern implements ImageProcessor<FImage> {
	protected int[][] pattern;
	protected float radius;
	protected int samples;
	
	/**
	 * Construct an extended LBP extractor with the given parameters.
	 * @param radius the radius of the sampling circle
	 * @param samples the number of samples around the circle
	 */
	public ExtendedLocalBinaryPattern(float radius, int samples) {
		checkParams(radius, samples);
		this.radius = radius;
		this.samples = samples;
	}
	
	/**
	 * Calculate the LBP for every pixel in the image. The returned
	 * array of LBP codes hase the same dimensions as the image. 
	 * 
	 * Samples taken from outside the image bounds are assumed to have 
	 * the value 0.
	 * 
	 * @param image the image
	 * @param radius the radius of the sampling circle
	 * @param samples the number of samples around the circle
	 * @return a 2d-array of the LBP codes for every pixel
	 */
	public static int[][] calculateLBP(FImage image, float radius, int samples) {
		checkParams(radius, samples);
		
		int [][] pattern = new int[image.height][image.width];
		
		for (int y=0; y<image.height; y++) {
			for (int x=0; x<image.width; x++) {
				pattern[y][x] = calculateLBP(image, radius, samples, x, y);
			}
		}
		
		return pattern;
	}
	
	/**
	 * Calculate the extended LBP for a single point. The
	 * point must be within the image.
	 * 
	 * @param image the image
	 * @param radius the radius of the sampling circle
	 * @param samples the number of samples around the circle
	 * @param x the x-coordinate of the point
	 * @param y the y-coordinate of the point
	 * @return the LBP code
	 */
	public static int calculateLBP(FImage image, float radius, int samples, int x, int y) {
		float centre = image.pixels[y][x];
		int pattern = 0;
		
		for (int i=0; i<samples; i++) {
			double xx = -radius * Math.sin(2 * Math.PI * i / samples);
			double yy = radius * Math.cos(2 * Math.PI * i / samples);
			
			float pix = image.getPixelInterp(xx, yy);
			
			if (pix - centre >= 0) {
				pattern += Math.pow(2, i);
			}
		}
		
		return pattern;
	}

	/**
	 * Calculate the extended LBP for a single point. The
	 * point must be within the image.
	 * 
	 * @param image the image
	 * @param radius the radius of the sampling circle
	 * @param samples the number of samples around the circle
	 * @param point the point
	 * @return the LBP code
	 */
	public static int calculateLBP(FImage image, float radius, int samples, Pixel point) {
		return calculateLBP(image, radius, samples, point.x, point.y);
	}

	private static void checkParams(float radius, int samples) {
		if (radius <= 0) {
			throw new IllegalArgumentException("radius must be greater than 0");
		}
		if (samples <= 1 || samples > 31) {
			throw new IllegalArgumentException("samples cannot be less than one or more than 31");
		}
	}

	@Override
	public void processImage(FImage image, Image<?, ?>... otherimages) {
		pattern = calculateLBP(image, radius, samples);
	}
	
	/**
	 * Get the pattern created during the last call to
	 * {@link #processImage(FImage, Image[])}.
	 * 
	 * @return the pattern
	 */
	public int[][] getPattern() {
		return pattern;
	}
}
