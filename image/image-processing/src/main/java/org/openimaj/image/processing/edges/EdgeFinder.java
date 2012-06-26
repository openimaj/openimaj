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
package org.openimaj.image.processing.edges;

import static java.lang.Math.atan;
import static java.lang.Math.sqrt;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;

/**
 * Using a simple sobel-like x and y derivative kernel, find edges in an image.
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class EdgeFinder implements ImageAnalyser<FImage> {
	protected float [][] kx;
	protected float [][] ky;

	/**
	 * The magnitudes of each edge as worked out by the hypotenuse of the triangle formed by the dx and dy of a pixel
	 */
	public FImage magnitude;
	/**
	 * The direction of each edge as worked out by the angle between the triangle formed by the dx and dy of a pixel
	 */
	public FImage angle;

	/**
	 * Find edges in an image using the following edge kernels
	 * @param kx
	 * @param ky
	 */
	public EdgeFinder(float [][] kx, float [][] ky) {
		this.kx = kx;
		this.ky = ky;
	}

	/**
	 * Find the edges in an image using default, sobel like 5x5 derivative kernels
	 */
	public EdgeFinder() {
		kx = new float[][] {
				{+1, +1, 0, -1, -1},
				{+2, +2, 0, -2, -2},
				{+2, +2, 0, -2, -2},
				{+2, +2, 0, -2, -2},
				{+1, +1, 0, -1, -1}
		};

		ky = new float[][] {
				{+1, +2, +2, +2, +1},
				{+1, +2, +2, +2, +1},
				{ 0,  0,  0,  0,  0},
				{-1, -2, -2, -2, -1},
				{-1, -2, -2, -2, -1}
		};
	}

	/* (non-Javadoc)
	 * @see org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj.image.Image)
	 */
	@Override
	public void analyseImage(FImage image) {
		int height = image.getHeight();
		int width = image.getWidth();

		magnitude = new FImage(width, height);
		angle = new FImage(width, height);

		int w = (kx.length - 1) / 2;

		for (int y=w+1; y<height-w; y++) {
			for (int x=w+1; x<width-w; x++) {
				//compute gradient
				double gx = 0;
				double gy = 0;
				for (int j=0; j<kx.length; j++) {
					for (int i=0; i<kx.length; i++) {
						gx += image.pixels[y-w+j][x-w+i] * kx[j][i];
						gy += image.pixels[y-w+j][x-w+i] * ky[j][i];
					}
				}

				magnitude.pixels[y][x] = (float) sqrt(gx*gx + gy*gy);

				if(gy!=0)
					angle.pixels[y][x] = (float) atan(gx/-gy);
				else
					angle.pixels[y][x] = 1.57f;
			}
		}
	}	
}
