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
package org.openimaj.image.analysis.algorithm;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Implementation of an Integral Image or Summed Area Table. This Implementation
 * calculates both the sum and squared sum values.
 * <p>
 * See http://en.wikipedia.org/wiki/Summed_area_table and
 * http://research.microsoft
 * .com/en-us/um/people/viola/Pubs/Detect/violaJones_IJCV.pdf
 * <p>
 * Basically, this provides an efficient way to find the sum of all pixels in a
 * rectangular area of an image.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class SummedSqAreaTable implements ImageAnalyser<FImage> {
	/**
	 * The sum data
	 */
	public FImage sum;

	/**
	 * The squared sum data
	 */
	public FImage sqSum;

	/**
	 * Construct an empty SAT
	 */
	public SummedSqAreaTable() {
	}

	/**
	 * Construct a SAT from the provided image
	 * 
	 * @param image
	 *            the image
	 */
	public SummedSqAreaTable(FImage image) {
		computeTable(image);
	}

	protected void computeTable(FImage image) {
		sum = new FImage(image.getWidth() + 1, image.getHeight() + 1);
		sqSum = new FImage(image.getWidth() + 1, image.getHeight() + 1);

		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				final float p = image.pixels[y][x];

				sum.pixels[y + 1][x + 1] = p +
											sum.pixels[y + 1][x] +
											sum.pixels[y][x + 1] -
											sum.pixels[y][x];

				sqSum.pixels[y + 1][x + 1] = p * p +
										sqSum.pixels[y + 1][x] +
										sqSum.pixels[y][x + 1] -
										sqSum.pixels[y][x];
			}
		}
	}

	/**
	 * Calculate the sum of pixels in the image used for constructing this SAT
	 * within the rectangle defined by (x1,y1) [top-left coordinate] and (x2,y2)
	 * [bottom- right coordinate]
	 * 
	 * @param x1
	 *            x1
	 * @param y1
	 *            y1
	 * @param x2
	 *            x2
	 * @param y2
	 *            y2
	 * @return sum of pixels in given rectangle
	 */
	public float calculateSumArea(int x1, int y1, int x2, int y2) {
		final float A = sum.pixels[y1][x1];
		final float B = sum.pixels[y1][x2];
		final float C = sum.pixels[y2][x2];
		final float D = sum.pixels[y2][x1];

		return A + C - B - D;
	}

	/**
	 * Calculate the sum of pixels in the image used for constructing this SAT
	 * within the given rectangle
	 * 
	 * @param r
	 *            rectangle
	 * @return sum of pixels in given rectangle
	 */
	public float calculateSumArea(Rectangle r) {
		return calculateSumArea(Math.round(r.x), Math.round(r.y), Math.round(r.x + r.width), Math.round(r.y + r.height));
	}

	/**
	 * Calculate the sum of squared pixels in the image used for constructing
	 * this SAT within the rectangle defined by (x1,y1) [top-left coordinate]
	 * and (x2,y2) [bottom- right coordinate]
	 * 
	 * @param x1
	 *            x1
	 * @param y1
	 *            y1
	 * @param x2
	 *            x2
	 * @param y2
	 *            y2
	 * @return sum of pixels in given rectangle
	 */
	public float calculateSqSumArea(int x1, int y1, int x2, int y2) {
		final float A = sqSum.pixels[y1][x1];
		final float B = sqSum.pixels[y1][x2];
		final float C = sqSum.pixels[y2][x2];
		final float D = sqSum.pixels[y2][x1];

		return A + C - B - D;
	}

	/**
	 * Calculate the sum of squared pixels in the image used for constructing
	 * this SAT within the given rectangle
	 * 
	 * @param r
	 *            rectangle
	 * @return sum of pixels in given rectangle
	 */
	public float calculateSqSumArea(Rectangle r) {
		return calculateSqSumArea(Math.round(r.x), Math.round(r.y), Math.round(r.x + r.width), Math.round(r.y + r.height));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.image.analyser.ImageAnalyser#analyseImage(org.openimaj.image
	 * .Image)
	 */
	@Override
	public void analyseImage(FImage image) {
		computeTable(image);
	}
}
