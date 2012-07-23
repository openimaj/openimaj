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
 * A common set of objects, namely the gaussian pyramid for an image and the gradients of each
 * level of the pyramid
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class PyramidSet{
	/**
	 * @param image
	 * @param tc
	 */
	public PyramidSet(FImage image, TrackingContext tc) {
		int nrows = image.height, ncols = image.width;
		FImage floatimg2 = image.process(new FGaussianConvolve(tc.computeSmoothSigma()));
		this.imgPyr = new Pyramid(ncols, nrows, (int) tc.subsampling, tc.nPyramidLevels);
		this.imgPyr.computePyramid(floatimg2, tc.pyramid_sigma_fact);
		this.gradx = new Pyramid(ncols, nrows, (int) tc.subsampling, tc.nPyramidLevels);
		this.grady = new Pyramid(ncols, nrows, (int) tc.subsampling, tc.nPyramidLevels);
		for (int i = 0 ; i < tc.nPyramidLevels ; i++)
			tc.computeGradients(imgPyr.img[i], tc.grad_sigma, gradx.img[i], grady.img[i]);
	}
	/**
	 * @param imgPyr
	 * @param gradx
	 * @param grady
	 */
	public PyramidSet(Pyramid imgPyr, Pyramid gradx,Pyramid grady) {
		this.imgPyr = imgPyr;
		this.gradx = gradx;
		this.grady = grady;
	}
	PyramidSet() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * the image pyramid
	 */
	public Pyramid imgPyr;
	/**
	 * the x gradient pyramid
	 */
	public Pyramid gradx;
	/**
	 * the y gradient pyramid
	 */
	public Pyramid grady;
	
	/**
	 * @return true if the pyramid is null
	 */
	public boolean isNull() {
		return imgPyr == null || grady == null || gradx == null;
	}
}