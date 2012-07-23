/**
 * This source code file is part of a direct port of Stan Birchfield's implementation
 * of a Kanade-Lucas-Tomasi feature tracker. The original implementation can be found
 * here: http://www.ces.clemson.edu/~stb/klt/
 *
 * As per the original code, the source code is in the public domain, available
 * for both commercial and non-commercial use.
 */
package org.openimaj.video.tracking.klt;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.convolution.FGaussianConvolve;

/**
 * A simple Gaussian pyramid
 * 
 * @author Stan Birchfield
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class Pyramid {
	int subsampling;
	int nLevels;
	FImage [] img;
	int [] ncols, nrows;

	/**
	 * @param ncols
	 * @param nrows
	 * @param subsampling
	 * @param nlevels
	 */
	public Pyramid(int ncols, int nrows, int subsampling, int nlevels) {
		if (subsampling != 2 && subsampling != 4 && 
				subsampling != 8 && subsampling != 16 && subsampling != 32)
			throw new RuntimeException("(_KLTCreatePyramid)  Pyramid's subsampling must be either 2, 4, 8, 16, or 32");

		img = new FImage[nlevels];
		this.ncols = new int[nlevels];
		this.nrows = new int[nlevels];

		/* Set parameters */
		this.subsampling = subsampling;
		this.nLevels = nlevels;

		/* Allocate memory for each level of pyramid and assign pointers */
		for (int i = 0 ; i < nlevels ; i++) {
			this.img[i] =  new FImage(ncols, nrows);
			this.ncols[i] = ncols;  
			this.nrows[i] = nrows;
			ncols /= subsampling;  
			nrows /= subsampling;
		}
	}

	/*********************************************************************
	 * 
	 */
	void computePyramid(FImage img, float sigma_fact) {
		FImage currimg, tmpimg;
		int ncols = img.width, nrows = img.height;
		int subsampling = this.subsampling;
		int subhalf = subsampling / 2;
		float sigma = subsampling * sigma_fact;  /* empirically determined */
		int i, x, y;

		if (subsampling != 2 && subsampling != 4 && 
				subsampling != 8 && subsampling != 16 && subsampling != 32)
			throw new RuntimeException("(_KLTComputePyramid)  Pyramid's subsampling must be either 2, 4, 8, 16, or 32");

		/* Copy original image to level 0 of pyramid */
		this.img[0] = img.clone();

		currimg = img;
		for (i = 1 ; i < this.nLevels ; i++)  {
			//tmpimg = FImage(nrows, ncols);
			//_KLTComputeSmoothedImage(currimg, sigma, tmpimg);
			tmpimg = currimg.process(new FGaussianConvolve(sigma));

			/* Subsample */
			ncols /= subsampling;  nrows /= subsampling;
			for (y = 0 ; y < nrows ; y++)
				for (x = 0 ; x < ncols ; x++)
					this.img[i].pixels[y][x] = tmpimg.pixels[(subsampling*y+subhalf)][(subsampling*x+subhalf)];

			/* Reassign current image */
			currimg = this.img[i];
		}
	}
}
