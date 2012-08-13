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
 * Implementation of an extended local binary pattern which has a
 * variable number of samples taken from a variable sized circle
 * about a point.
 *  
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Ojala, Timo", "Pietik\"{a}inen, Matti", "M\"{a}enp\"{a}\"{a}, Topi" },
		title = "Multiresolution Gray-Scale and Rotation Invariant Texture Classification with Local Binary Patterns",
		year = "2002",
		journal = "IEEE Trans. Pattern Anal. Mach. Intell.",
		pages = { "971", "", "987" },
		url = "http://dx.doi.org/10.1109/TPAMI.2002.1017623",
		month = "July",
		number = "7",
		publisher = "IEEE Computer Society",
		volume = "24",
		customData = {
			"date", "July 2002",
			"issn", "0162-8828",
			"numpages", "17",
			"doi", "10.1109/TPAMI.2002.1017623",
			"acmid", "628808",
			"address", "Washington, DC, USA"
		}
	)
public class ExtendedLocalBinaryPattern implements ImageAnalyser<FImage> {
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
			double dx = -radius * Math.sin(2 * Math.PI * i / samples);
			double dy = radius * Math.cos(2 * Math.PI * i / samples);
			
			float pix = image.getPixelInterp(x+dx, y+dy);
			
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

	/* (non-Javadoc)
	 * @see org.openimaj.image.analyser.ImageAnalyser#analyseImage(org.openimaj.image.Image)
	 */
	@Override
	public void analyseImage(FImage image) {
		pattern = calculateLBP(image, radius, samples);
	}
	
	/**
	 * Get the pattern created during the last call to
	 * {@link #analyseImage(FImage)}.
	 * 
	 * @return the pattern
	 */
	public int[][] getPattern() {
		return pattern;
	}
}
