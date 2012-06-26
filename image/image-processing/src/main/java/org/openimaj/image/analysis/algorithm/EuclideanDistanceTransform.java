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

import java.io.File;
import java.io.IOException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.analyser.ImageAnalyser;

/**
 * See http://people.cs.uchicago.edu/~pff/papers/dt.pdf
 * 
 * An efficient euclidean distance transform applicable to all greyscale images. The distance of each pixel to the closest 
 * valid pixel is given. In this case a pixel is considered valid when it is less than Float.MAX_VALUE.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class EuclideanDistanceTransform implements ImageAnalyser<FImage> {
	FImage distances;
	int [][] indices;

	/* (non-Javadoc)
	 * @see org.openimaj.image.analyser.ImageAnalyser#analyseImage(org.openimaj.image.Image)
	 */
	@Override
	public void analyseImage(FImage image) {
		if (distances == null || distances.height != image.height || distances.width != distances.height) {
			distances = new FImage(image.width, image.height);
			indices = new int[image.height][image.width];
		}

		squaredEuclideanDistance(image, distances, indices);
	}

	/**
	 * @return the distance transformed image (unnormalised)
	 */
	public FImage getDistances() {
		return distances;
	}

	/**
	 * @return the indecies of the closest pixel to any given pixel
	 */
	public int[][] getIndices() {
		return indices;
	}

	protected static void DT1D(float [] f, float [] d, int [] v, int [] l, float [] z) {
		int k = 0;

		v[0] = 0;
		z[0] = -Float.MAX_VALUE;
		z[1] = Float.MAX_VALUE;

		for (int q = 1; q < f.length; q++)
		{
			float s  = ((f[q] + q * q) - (f[v[k]] + v[k] * v[k])) / (2 * q - 2 * v[k]);

			while (s <= z[k]) {
				k--;
				s  = ((f[q] + q * q) - (f[v[k]] + v[k] * v[k])) / (2 * q - 2 * v[k]);
			}
			k++;
			v[k] = q;
			z[k] = s;
			z[k + 1] = Float.MAX_VALUE;
		}

		k = 0;
		for (int q = 0; q < f.length; q++)
		{
			while (z[k + 1] < q)
				k++;

			d[q] = (q - v[k]) * (q - v[k]) + f[v[k]];
			l[q] = v[k];
		}
	}

	/**
	 * Calculate the squared euclidean distance transform of a binary image with
	 * foreground pixels set to 1 and background set to 0.
	 * @param image the image to be transformed.
	 * @param distances the distance of each pixel to the closest 1-pixel
	 * @param indices the index of the closes valid pixel
	 */
	public static void squaredEuclideanDistanceBinary(FImage image, FImage distances, int[][] indices) {
		float [] f = new float[Math.max(image.height, image.width)];
		float [] d = new float[f.length];
		int [] v = new int[f.length];
		int [] l = new int[f.length];
		float [] z = new float[f.length + 1];

		for (int x=0; x<image.width; x++) {
			for (int y=0; y<image.height; y++) {
				f[y] = image.pixels[y][x] == 0 ? Float.MAX_VALUE : 0;
			}

			DT1D(f, d, v, l, z);
			for (int y = 0; y < image.height; y++) {
				distances.pixels[y][x] = d[y];
				indices[y][x] = (l[y] * image.width) + x; //this is now row-major
			}
		}
		
		for (int y = 0; y < image.height; y++) {
			DT1D(distances.pixels[y], d, v, l, z);

			for (int x = 0; x < image.width; x++)
				l[x] = indices[y][l[x]];

			for (int x = 0; x < image.width; x++)
			{
				distances.pixels[y][x] = d[x];
				indices[y][x] = l[x];
			}
		}
	}
	
	/**
	 * The static function which underlies EuclideanDistanceTransform. Provide an image, fill distances and indices with 
	 * the distance image and the closest pixel indices. Typically, for the binary case, valid pixels are set to 0 and invalid
	 * pixels are set to Float.MAX_VALUE or Float.POSITIVE_INFINITY.
	 * 
	 * @param image the image to be transformed. Each pixel is considered valid except those of value Float.MAX_VALUE
	 * @param distances the distance of each pixel to the closest non-Float.MAX_VALUE pixel
	 * @param indices the index of the closes valid pixel
	 */
	public static void squaredEuclideanDistance(FImage image, FImage distances, int[][] indices) {
		float [] f = new float[Math.max(image.height, image.width)];
		float [] d = new float[f.length];
		int [] v = new int[f.length];
		int [] l = new int[f.length];
		float [] z = new float[f.length + 1];

		for (int x=0; x<image.width; x++) {
			for (int y=0; y<image.height; y++) {
				f[y] = Float.isInfinite(image.pixels[y][x]) ? (image.pixels[y][x] > 0 ? Float.MAX_VALUE : -Float.MAX_VALUE) : image.pixels[y][x];
			}

			DT1D(f, d, v, l, z);
			for (int y = 0; y < image.height; y++) {
				distances.pixels[y][x] = d[y];
				indices[y][x] = (l[y] * image.width) + x; //this is now row-major
			}
		}
		
		for (int y = 0; y < image.height; y++) {
			DT1D(distances.pixels[y], d, v, l, z);

			for (int x = 0; x < image.width; x++)
				l[x] = indices[y][l[x]];

			for (int x = 0; x < image.width; x++)
			{
				distances.pixels[y][x] = d[x];
				indices[y][x] = l[x];
			}
		}
	}
	
	/**
	 * Test the distance transform
	 * @param args
	 * @throws IOException
	 */
	public static void main(String args[]) throws IOException{
		FImage i = ImageUtilities.readF(new File("/Users/ss/Desktop/tache.jpg"));
		EuclideanDistanceTransform etrans = new EuclideanDistanceTransform();
//		i.processInplace(new CannyEdgeDetector());
		i.inverse();
		for(int x = 0;x < i.width; x++)
			for(int y = 0; y < i.height; y++) 
				if(i.pixels[y][x] == 1.0f) 
					i.setPixel(x, y, Float.MAX_VALUE);
		DisplayUtilities.display(i);
		i.analyseWith(etrans);
		i = etrans.getDistances();
		i.normalise();
		DisplayUtilities.display(i);
	}
}
