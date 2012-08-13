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
 * Implementation of the original 3x3 form of a local binary pattern.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Ojala, T.", "Pietikainen, M.", "Harwood, D." },
		title = "A Comparative Study of Texture Measures with Classification Based on Feature Distributions",
		year = "1996",
		journal = "Pattern Recognition",
		pages = { "51", "59" },
		month = "January",
		number = "1",
		volume = "29"
	)
public class BasicLocalBinaryPattern implements ImageAnalyser<FImage> {
	protected int[][] pattern = null;
	
	/**
	 * Get the pattern created during the last call to
	 * {@link #analyseImage(FImage)}.
	 * 
	 * @return the pattern
	 */
	public int[][] getPattern() {
		return pattern;
	}
	
	/**
	 * Calculate the LBP for every pixel in the image. The returned
	 * array of LBP codes has the same dimensions as the image. 
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

	/* (non-Javadoc)
	 * @see org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj.image.Image)
	 */
	@Override
	public void analyseImage(FImage image) {
		pattern = calculateLBP(image);
	}
}
