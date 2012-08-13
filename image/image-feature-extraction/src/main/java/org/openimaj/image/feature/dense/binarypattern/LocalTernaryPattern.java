/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.image.feature.dense.binarypattern;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.pixel.Pixel;

/**
 * Implementation of a Local Ternary Pattern.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Tan, Xiaoyang", "Triggs, Bill" },
		title = "Enhanced local texture feature sets for face recognition under difficult lighting conditions",
		year = "2010",
		journal = "Trans. Img. Proc.",
		pages = { "1635", "", "1650" },
		url = "http://dx.doi.org/10.1109/TIP.2010.2042645",
		month = "June",
		number = "6",
		publisher = "IEEE Press",
		volume = "19"
	)
public class LocalTernaryPattern implements ImageAnalyser<FImage> {
	protected int[][] positiveBinaryPattern;
	protected int[][] negativeBinaryPattern;
	protected int[][] ternaryPattern;
	
	protected float radius;
	protected int samples;
	protected float threshold;
	
	/**
	 * Construct an LTP extractor with the given parameters.
	 * @param radius the radius of the sampling circle
	 * @param samples the number of samples around the circle
	 * @param threshold the threshold
	 */
	public LocalTernaryPattern(float radius, int samples, float threshold) {
		checkParams(radius, samples);
		this.radius = radius;
		this.samples = samples;
		this.threshold = threshold;
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
	 * @param threshold the threshold
	 * @return three 2d-arrays of the positive and negative binary LTP codes and the ternary code for every pixel
	 */
	public static int[][][] calculateLTP(FImage image, float radius, int samples, float threshold) {
		checkParams(radius, samples);
		
		int [][][] pattern = new int[3][image.height][image.width];
		
		for (int y=0; y<image.height; y++) {
			for (int x=0; x<image.width; x++) {
				int [] pn = calculateLTP(image, radius, samples, threshold, x, y);
				pattern[0][y][x] = pn[0];
				pattern[1][y][x] = pn[1];
				pattern[2][y][x] = pn[2];
			}
		}
		
		return pattern;
	}
	
	/**
	 * Calculate the LTP for a single point. The
	 * point must be within the image.
	 * 
	 * @param image the image
	 * @param radius the radius of the sampling circle
	 * @param samples the number of samples around the circle
	 * @param threshold the threshold
	 * @param x the x-coordinate of the point
	 * @param y the y-coordinate of the point
	 * @return the LTP code (positive and negative binary code and ternary code)
	 */
	public static int[] calculateLTP(FImage image, float radius, int samples, float threshold, int x, int y) {
		float centre = image.pixels[y][x];
		int pattern[] = new int[3];
		
		for (int i=0; i<samples; i++) {
			double xx = -radius * Math.sin(2 * Math.PI * i / samples);
			double yy = radius * Math.cos(2 * Math.PI * i / samples);
			
			float pix = image.getPixelInterp(x+xx, y+yy);
			
			float d = pix - centre;
			
			if (d >= threshold) {
				pattern[0] += Math.pow(2, i);
				pattern[2] += Math.pow(3, i);
			}
			if (d <= threshold) {
				pattern[1] += Math.pow(2, i);
				pattern[2] += 2 * Math.pow(3, i);
			}
		}
		
		return pattern;
	}

	/**
	 * Calculate the LTP for a single point. The
	 * point must be within the image.
	 * 
	 * @param image the image
	 * @param radius the radius of the sampling circle
	 * @param threshold the threshold
	 * @param samples the number of samples around the circle
	 * @param point the point
	 * @return the LTP code (positive and negative binary code and ternary code)
	 */
	public static int [] calculateLTP(FImage image, float radius, int samples, float threshold, Pixel point) {
		return calculateLTP(image, radius, samples, threshold, point.x, point.y);
	}

	private static void checkParams(float radius, int samples) {
		if (radius <= 0) {
			throw new IllegalArgumentException("radius must be greater than 0");
		}
		if (samples <= 1 || samples > 31) {
			throw new IllegalArgumentException("samples cannot be less than one or more than 31");
		}
	}

	/* (non-Javadoc)
	 * @see org.openimaj.image.analyser.ImageAnalyser#analyseImage(org.openimaj.image.Image)
	 */
	@Override
	public void analyseImage(FImage image) {
		int [][][] patterns = calculateLTP(image, radius, samples, threshold);
		
		positiveBinaryPattern = patterns[0];
		negativeBinaryPattern = patterns[1];
		ternaryPattern = patterns[2];
	}
	
	/**
	 * Get the positive pattern created during the last call to
	 * {@link #analyseImage(FImage)}.
	 * 
	 * @return the pattern
	 */
	public int[][] getPositivePattern() {
		return positiveBinaryPattern;
	}
	
	/**
	 * Get the negative pattern created during the last call to
	 * {@link #analyseImage(FImage)}.
	 * 
	 * @return the pattern
	 */
	public int[][] getNegativePattern() {
		return negativeBinaryPattern;
	}
	
	/**
	 * Get the ternary pattern created during the last call to
	 * {@link #analyseImage(FImage)}.
	 * 
	 * @return the pattern
	 */
	public int[][] getTernaryPattern() {
		return ternaryPattern;
	}
}
