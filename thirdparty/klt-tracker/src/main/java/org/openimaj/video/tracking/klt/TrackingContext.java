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
import org.openimaj.math.geometry.shape.Shape;


/*********************************************************************
 * klt.c
 *
 * Kanade-Lucas-Tomasi tracker
 *********************************************************************/
public class TrackingContext {
	protected final static int _mindist = 10;
	protected final static int _window_size = 7;
	protected final static int _min_eigenvalue = 1;
	protected final static float _min_determinant = 0.01f;
	protected final static float _min_displacement = 0.1f;
	protected final static int _max_iterations = 10;
	protected final static float _max_residue = 10.0f;
	protected final static float _grad_sigma = 1.0f;
	protected final static float _smooth_sigma_fact = 0.1f;
	protected final static float _pyramid_sigma_fact = 0.9f;
	protected final static float _step_factor = 1.0f;
	protected static boolean _sequentialMode = false;

	protected final static boolean _lighting_insensitive = false;
	/* for affine mapping*/
	protected final static int _affineConsistencyCheck = -1;
	protected final static int _affine_window_size = 15;
	protected final static int _affine_max_iterations = 10;
	protected final static float _affine_max_residue = 10.0f;
	protected final static float _affine_min_displacement = 0.02f;
	protected final static float _affine_max_displacement_differ = 1.5f;

	protected final static boolean _smoothBeforeSelecting = true;
	protected final static boolean _writeInternalImages = false;
	protected final static int _search_range = 15;
	protected final static int _nSkippedPixels = 0;

	/* Available to user */
	int mindist;			/* min distance b/w features */
	int window_width, window_height;
	boolean sequentialMode;	/* whether to save most recent image to save time */
	/* can set to TRUE manually, but don't set to */
	/* FALSE manually */
	boolean smoothBeforeSelecting;	/* whether to smooth image before */
	/* selecting features */
	boolean writeInternalImages;	/* whether to write internal images */

	/* tracking features */
	boolean lighting_insensitive;  /* whether to normalize for gain and bias (not in original algorithm) */


	/* Available, but hopefully can ignore */
	int min_eigenvalue;		/* smallest eigenvalue allowed for selecting */
	float min_determinant;	/* th for determining lost */
	float min_displacement;	/* th for stopping tracking when pixel changes little */
	int max_iterations;		/* th for stopping tracking when too many iterations */
	float max_residue;		/* th for stopping tracking when residue is large */
	float grad_sigma;
	float smooth_sigma_fact;
	float pyramid_sigma_fact;
	float step_factor;  /* size of Newton steps; 2.0 comes from equations, 1.0 seems to avoid overshooting */
	int nSkippedPixels;		/* # of pixels skipped when finding features */
	int borderx;			/* border in which features will not be found */
	int bordery;
	int nPyramidLevels;		/* computed from search_ranges */
	int subsampling;		/* 		" */

	/* for affine mapping */ 
	int affine_window_width, affine_window_height;
	int affineConsistencyCheck; /* whether to evaluates the consistency of features with affine mapping 
					 -1 = don't evaluates the consistency
					 0 = evaluates the consistency of features with translation mapping
					 1 = evaluates the consistency of features with similarity mapping
					 2 = evaluates the consistency of features with affine mapping
	 */
	int affine_max_iterations;  
	float affine_max_residue;
	float affine_min_displacement;        
	float affine_max_displacement_differ; /* th for the difference between the displacement calculated 
						   by the affine tracker and the frame to frame tracker in pel*/
	
	private Shape targetArea = null;

	/* User must not touch these */
	private Pyramid pyramid_last;
	private Pyramid pyramid_last_gradx;
	private Pyramid pyramid_last_grady;
	
	/**
	 * @return a {@link PyramidSet} of the previous image's pyramids. Null if not previous image
	 */
	public PyramidSet previousPyramidSet(){
		if(pyramid_last == null)
			return null;
		else
			return new PyramidSet(pyramid_last,pyramid_last_gradx,pyramid_last_grady);
	}

	/*********************************************************************
	 * KLTCreateTrackingContext
	 *
	 */

	public TrackingContext()
	{
		/* Set values to default values */
		this.mindist = _mindist;
		this.window_width = _window_size;
		this.window_height = _window_size;
		this.sequentialMode = _sequentialMode;
		this.smoothBeforeSelecting = _smoothBeforeSelecting;
		this.writeInternalImages = _writeInternalImages;

		this.lighting_insensitive = _lighting_insensitive;
		this.min_eigenvalue = _min_eigenvalue;
		this.min_determinant = _min_determinant;
		this.max_iterations = _max_iterations;
		this.min_displacement = _min_displacement;
		this.max_residue = _max_residue;
		this.grad_sigma = _grad_sigma;
		this.smooth_sigma_fact = _smooth_sigma_fact;
		this.pyramid_sigma_fact = _pyramid_sigma_fact;
		this.step_factor = _step_factor;
		this.nSkippedPixels = _nSkippedPixels;
		this.pyramid_last = null;
		this.pyramid_last_gradx = null;
		this.pyramid_last_grady = null;
		/* for affine mapping */
		this.affineConsistencyCheck = _affineConsistencyCheck;
		this.affine_window_width = _affine_window_size;
		this.affine_window_height = _affine_window_size;
		this.affine_max_iterations = _affine_max_iterations;
		this.affine_max_residue = _affine_max_residue;
		this.affine_min_displacement = _affine_min_displacement;
		this.affine_max_displacement_differ = _affine_max_displacement_differ;

		/* Change nPyramidLevels and subsampling */
		changeTCPyramid(_search_range);

		/* Update border, which is dependent upon */
		/* smooth_sigma_fact, pyramid_sigma_fact, window_size, and subsampling */
		updateTCBorder();
	}

	/*********************************************************************
	 * KLTPrintTrackingContext
	 */
	@Override
	public String toString()
	{
		String s = "";
		s += String.format("\n\nTracking context:\n\n");
		s += String.format("\tmindist = %d\n", this.mindist);
		s += String.format("\twindow_width = %d\n", this.window_width);
		s += String.format("\twindow_height = %d\n", this.window_height);
		s += String.format("\tsequentialMode = %s\n", this.sequentialMode ? "true" : "false");
		s += String.format("\tsmoothBeforeSelecting = %s\n", this.smoothBeforeSelecting ? "true" : "false");
		s += String.format("\twriteInternalImages = %s\n", this.writeInternalImages ? "true" : "false");

		s += String.format("\tmin_eigenvalue = %d\n", this.min_eigenvalue);
		s += String.format("\tmin_determinant = %f\n", this.min_determinant);
		s += String.format("\tmin_displacement = %f\n", this.min_displacement);
		s += String.format("\tmax_iterations = %d\n", this.max_iterations);
		s += String.format("\tmax_residue = %f\n", this.max_residue);
		s += String.format("\tgrad_sigma = %f\n", this.grad_sigma);
		s += String.format("\tsmooth_sigma_fact = %f\n", this.smooth_sigma_fact);
		s += String.format("\tpyramid_sigma_fact = %f\n", this.pyramid_sigma_fact);
		s += String.format("\tnSkippedPixels = %d\n", this.nSkippedPixels);
		s += String.format("\tborderx = %d\n", this.borderx);
		s += String.format("\tbordery = %d\n", this.bordery);
		s += String.format("\tnPyramidLevels = %d\n", this.nPyramidLevels);
		s += String.format("\tsubsampling = %d\n", this.subsampling);

		s += String.format("\n\tpyramid_last = %s\n", (this.pyramid_last!=null) ? "points to old image" : "null");
		s += String.format("\tpyramid_last_gradx = %s\n", (this.pyramid_last_gradx!=null) ? "points to old image" : "null");
		s += String.format("\tpyramid_last_grady = %s\n", (this.pyramid_last_grady!=null) ? "points to old image" : "null");
		s += String.format("\n\n");

		return s;
	}


	/*********************************************************************
	 * KLTChangeTCPyramid
	 * @param search_range 
	 *
	 */
	public void changeTCPyramid(int search_range) {
		float window_halfwidth;
		float subsampling;

		/* Check window size (and correct if necessary) */
		if (this.window_width % 2 != 1) {
			this.window_width = this.window_width+1;
			System.err.format("(KLTChangeTCPyramid) Window width must be odd. Changing to %d.\n", this.window_width);
		}
		if (this.window_height % 2 != 1) {
			this.window_height = this.window_height+1;
			System.err.format("(KLTChangeTCPyramid) Window height must be odd. Changing to %d.\n", this.window_height);
		}
		if (this.window_width < 3) {
			this.window_width = 3;
			System.err.format("(KLTChangeTCPyramid) Window width must be at least three. \nChanging to %d.\n", this.window_width);
		}
		if (this.window_height < 3) {
			this.window_height = 3;
			System.err.format("(KLTChangeTCPyramid) Window height must be at least three. \nChanging to %d.\n", this.window_height);
		}
		window_halfwidth = Math.min(this.window_width,this.window_height)/2.0f;

		subsampling = search_range / window_halfwidth;

		if (subsampling < 1.0) {		/* 1.0 = 0+1 */
			this.nPyramidLevels = 1;
		} else if (subsampling <= 3.0) {	/* 3.0 = 2+1 */
			this.nPyramidLevels = 2;
			this.subsampling = 2;
		} else if (subsampling <= 5.0) {	/* 5.0 = 4+1 */
			this.nPyramidLevels = 2;
			this.subsampling = 4;
		} else if (subsampling <= 9.0) {	/* 9.0 = 8+1 */
			this.nPyramidLevels = 2;
			this.subsampling = 8;
		} else {
			/* The following lines are derived from the formula:
	    search_range = 
	    window_halfwidth * \sum_{i=0}^{nPyramidLevels-1} 8^i,
	    which is the same as:
	    search_range = 
	    window_halfwidth * (8^nPyramidLevels - 1)/(8 - 1).
	    Then, the value is rounded up to the nearest integer. */
			float val = (float) (Math.log(7.0*subsampling+1.0)/Math.log(8.0));
			this.nPyramidLevels = (int) (val + 0.99);
			this.subsampling = 8;
		}
	}


	/*********************************************************************
	 * NOTE: Manually must ensure consistency with _KLTComputePyramid()
	 */
	float _pyramidSigma()
	{
		return (this.pyramid_sigma_fact * this.subsampling);
	}


	/*********************************************************************
	 * Updates border, which is dependent upon 
	 * smooth_sigma_fact, pyramid_sigma_fact, window_size, and subsampling
	 */

	public void updateTCBorder() {
		float val;
		int pyramid_gauss_hw;
		int smooth_gauss_hw;
		int gauss_width;
		int num_levels = this.nPyramidLevels;
		int n_invalid_pixels;
		int window_hw;
		int ss = this.subsampling;
		int ss_power;
		int border;
		int i;

		/* Check window size (and correct if necessary) */
		if (this.window_width % 2 != 1) {
			this.window_width = this.window_width+1;
			System.err.format("(KLTUpdateTCBorder) Window width must be odd. Changing to %d.\n", this.window_width);
		}
		if (this.window_height % 2 != 1) {
			this.window_height = this.window_height+1;
			System.err.format("(KLTUpdateTCBorder) Window height must be odd. Changing to %d.\n", this.window_height);
		}
		if (this.window_width < 3) {
			this.window_width = 3;
			System.err.format("(KLTUpdateTCBorder) Window width must be at least three. \nChanging to %d.\n", this.window_width);
		}
		if (this.window_height < 3) {
			this.window_height = 3;
			System.err.format("(KLTUpdateTCBorder) Window height must be at least three. \nChanging to %d.\n", this.window_height);
		}
		window_hw = Math.max(this.window_width, this.window_height)/2;

		/* Find widths of convolution windows */
		gauss_width=_getKernelWidths(computeSmoothSigma())[0];
		smooth_gauss_hw = gauss_width/2;
		
		gauss_width = _getKernelWidths(_pyramidSigma())[0];
		pyramid_gauss_hw = gauss_width/2;

		/* Compute the # of invalid pixels at each level of the pyramid.
	   n_invalid_pixels is computed with respect to the ith level  
	   of the pyramid. So, e.g., if n_invalid_pixels = 5 after  
	   the first iteration, then there are 5 invalid pixels in  
	   level 1, which translated means 5*subsampling invalid pixels  
	   in the original level 0. */
		n_invalid_pixels = smooth_gauss_hw;
		for (i = 1 ; i < num_levels ; i++) {
			val = ((float) n_invalid_pixels + pyramid_gauss_hw) / ss;
			n_invalid_pixels = (int) (val + 0.99); /* Round up */
		}

		/* ss_power = ss^(num_levels-1) */
		ss_power = 1;
		for (i = 1 ; i < num_levels ; i++)
			ss_power *= ss;

		/* Compute border by translating invalid pixels back into */
		/* original image */
		border = (n_invalid_pixels + window_hw) * ss_power;

		this.borderx = border;
		this.bordery = border;
	}

	/*********************************************************************
	 * KLTStopSequentialMode
	 */
	void stopSequentialMode()
	{
		this.sequentialMode = false;

		this.pyramid_last = null;
		this.pyramid_last_gradx = null;
		this.pyramid_last_grady = null;
	}

	float computeSmoothSigma() {
		return (smooth_sigma_fact * Math.max(window_width, window_height));
	}

	private class ConvolutionKernel {
		private static final int MAX_KERNEL_WIDTH = 71;

		int width;
		float [] data = new float[MAX_KERNEL_WIDTH];
	}

	ConvolutionKernel gauss_kernel = new ConvolutionKernel();
	ConvolutionKernel gaussderiv_kernel = new ConvolutionKernel();
	float sigma_last = -10.0f;

	/*********************************************************************
	 * _computeKernels
	 */
	void _computeKernels(float sigma, ConvolutionKernel gauss, ConvolutionKernel gaussderiv) {
		final float factor = 0.01f;   /* for truncating tail */
		int i;

		/* Compute kernels, and automatically determine widths */
		{
			final int hw = ConvolutionKernel.MAX_KERNEL_WIDTH / 2;
			float max_gauss = 1.0f, max_gaussderiv = (float) (sigma*Math.exp(-0.5f));

			/* Compute gauss and deriv */
			for (i = -hw ; i <= hw ; i++)  {
				gauss.data[i+hw]      = (float) Math.exp(-i*i / (2*sigma*sigma));
				gaussderiv.data[i+hw] = -i * gauss.data[i+hw];
			}

			/* Compute widths */
			gauss.width = ConvolutionKernel.MAX_KERNEL_WIDTH;
			for (i = -hw ; Math.abs(gauss.data[i+hw] / max_gauss) < factor ; i++) gauss.width -= 2;
			gaussderiv.width = ConvolutionKernel.MAX_KERNEL_WIDTH;
			for (i = -hw ; Math.abs(gaussderiv.data[i+hw] / max_gaussderiv) < factor ; i++) gaussderiv.width -= 2;
			if (gauss.width == ConvolutionKernel.MAX_KERNEL_WIDTH || gaussderiv.width == ConvolutionKernel.MAX_KERNEL_WIDTH)
				throw new RuntimeException(
						String.format("(_computeKernels) MAX_KERNEL_WIDTH %d is too small for a sigma of %f", ConvolutionKernel.MAX_KERNEL_WIDTH, sigma)
				);
		}

		/* Shift if width less than MAX_KERNEL_WIDTH */
		for (i = 0 ; i < gauss.width ; i++)
			gauss.data[i] = gauss.data[i+(ConvolutionKernel.MAX_KERNEL_WIDTH-gauss.width)/2];
		for (i = 0 ; i < gaussderiv.width ; i++)
			gaussderiv.data[i] = gaussderiv.data[i+(ConvolutionKernel.MAX_KERNEL_WIDTH-gaussderiv.width)/2];
		/* Normalize gauss and deriv */
		{
			final int hw = gaussderiv.width / 2;
			float den;

			den = 0.0f;
			for (i = 0 ; i < gauss.width ; i++)  den += gauss.data[i];
			for (i = 0 ; i < gauss.width ; i++)  gauss.data[i] /= den;
			den = 0.0f;
			for (i = -hw ; i <= hw ; i++)  den -= i*gaussderiv.data[i+hw];
			for (i = -hw ; i <= hw ; i++)  gaussderiv.data[i+hw] /= den;
		}

		sigma_last = sigma;
	}


	/*********************************************************************
	 * _KLTGetKernelWidths
	 *
	 */
	int [] _getKernelWidths(float sigma)
	{
		_computeKernels(sigma, gauss_kernel, gaussderiv_kernel);
		int gauss_width = gauss_kernel.width;
		int gaussderiv_width = gaussderiv_kernel.width;

		return new int[] {gauss_width, gaussderiv_width};
	}


	/*********************************************************************
	 * _convolveImageHoriz
	 */
	void _convolveImageHoriz(FImage imgin, ConvolutionKernel kernel, FImage imgout)
	{
		float sum;
		int radius = kernel.width / 2;
		int ncols = imgin.width, nrows = imgin.height;
		int i, j, k;

		/* Kernel width must be odd */
		assert(kernel.width % 2 == 1);

		/* Must read from and write to different images */
		assert(imgin != imgout);

		/* For each row, do ... */
		for (j = 0 ; j < nrows ; j++)  {
			int ptrout = 0;
			
			/* Zero leftmost columns */
			for (i = 0 ; i < radius ; i++)
				imgout.pixels[j][ptrout++] = 0.0f; //*ptrout++ = 0.0;

			/* Convolve middle columns with kernel */
			for ( ; i < ncols - radius ; i++)  {
				//ppp = ptrrow + i - radius;
				int ppp = i - radius;
				
				sum = 0.0f;
				for (k = kernel.width-1 ; k >= 0 ; k--)
					sum += imgin.pixels[j][ppp++] * kernel.data[k];//sum += *ppp++ * kernel.data[k];
				imgout.pixels[j][ptrout++] = sum;
			}

			/* Zero rightmost columns */
			for ( ; i < ncols ; i++)
				imgout.pixels[j][ptrout++] = 0.0f; //*ptrout++ = 0.0;
		}
	}


	/*********************************************************************
	 * _convolveImageVert
	 */
	void _convolveImageVert(FImage imgin, ConvolutionKernel kernel, FImage imgout) {
		float sum;
		int radius = kernel.width / 2;
		int ncols = imgin.width, nrows = imgin.height;
		int i, j, k;

		/* Kernel width must be odd */
		assert(kernel.width % 2 == 1);

		/* Must read from and write to different images */
		assert(imgin != imgout);

		/* For each column, do ... */
		for (i = 0 ; i < ncols ; i++)  {
			int ptrout = 0;
			/* Zero topmost rows */
			for (j = 0 ; j < radius ; j++)  {
				imgout.pixels[ptrout][i] = 0;
				ptrout++;
			}

			/* Convolve middle rows with kernel */
			for ( ; j < nrows - radius ; j++)  {
				int ppp = (j - radius);
				sum = 0.0f;
				for (k = kernel.width-1 ; k >= 0 ; k--)  {
					sum += imgin.pixels[ppp][i] * kernel.data[k];
					ppp++;
				}
				imgout.pixels[ptrout][i] = sum;
				ptrout ++;
			}

			/* Zero bottommost rows */
			for ( ; j < nrows ; j++)  {
				imgout.pixels[ptrout][i] = 0;
				ptrout++;
			}
		}
	}


	/*********************************************************************
	 * _convolveSeparate
	 */
	void _convolveSeparate(FImage imgin, ConvolutionKernel horiz_kernel, ConvolutionKernel vert_kernel, FImage imgout)
	{
		/* Create temporary image */
		FImage tmpimg = new FImage(imgin.width, imgin.height);

		/* Do convolution */
		_convolveImageHoriz(imgin, horiz_kernel, tmpimg);

		_convolveImageVert(tmpimg, vert_kernel, imgout);
	}

	/*********************************************************************
	 * _KLTComputeGradients
	 * @param img 
	 * @param sigma 
	 * @param gradx 
	 * @param grady 
	 */
	public void computeGradients(FImage img, float sigma, FImage gradx, FImage grady) {
		/* Compute kernels, if necessary */
		if (Math.abs(sigma - sigma_last) > 0.05)
			_computeKernels(sigma, gauss_kernel, gaussderiv_kernel);

		_convolveSeparate(img, gaussderiv_kernel, gauss_kernel, gradx);
		_convolveSeparate(img, gauss_kernel, gaussderiv_kernel, grady);
	}

	/**
	 * @return the minimum distance
	 */
	public int getMinDist() {
		return mindist;
	}

	/**
	 * Set the minimum distance
	 * @param mindist
	 */
	public void setMinDist(int mindist) {
		this.mindist = mindist;
	}

	/**
	 * @return the window width
	 */
	public int getWindowWidth() {
		return window_width;
	}

	/**
	 * Set the window width
	 * @param window_width
	 */
	public void setWindowWidth(int window_width) {
		this.window_width = window_width;
	}

	/**
	 * @return the window height
	 */
	public int getWindowHeight() {
		return window_height;
	}

	/**
	 * Set the window height
	 * @param window_height
	 */
	public void setWindowHeight(int window_height) {
		this.window_height = window_height;
	}

	/**
	 * @return true if in sequential mode; false otherwise.
	 */
	public boolean sequentialMode() {
		return sequentialMode;
	}

	/**
	 * Enable or disable sequential mode
	 * @param sequentialMode
	 */
	public void setSequentialMode(boolean sequentialMode) {
		this.sequentialMode = sequentialMode;
	}

	/**
	 * @return true if internal images are written; false otherwise
	 */
	public boolean writeInternalImages() {
		return writeInternalImages;
	}

	/**
	 * Enable or disable writing of internal images to disk
	 * @param writeInternalImages
	 */
	public void setWriteInternalImages(boolean writeInternalImages) {
		this.writeInternalImages = writeInternalImages;
	}

	/**
	 * @return true if lighting insensitivity is enabled; false otherwise.
	 */
	public boolean isLightingInsensitive() {
		return lighting_insensitive;
	}

	/**
	 * Enable or disable lighting insensitivity
	 * @param lighting_insensitive
	 */
	public void setLightingInsensitive(boolean lighting_insensitive) {
		this.lighting_insensitive = lighting_insensitive;
	}

	/**
	 * @return the minimum eigenvalue
	 */
	public int getMinEigenvalue() {
		return min_eigenvalue;
	}

	/**
	 * Set the minimum eigenvalue
	 * @param min_eigenvalue
	 */
	public void setMinEigenvalue(int min_eigenvalue) {
		this.min_eigenvalue = min_eigenvalue;
	}

	/**
	 * @return the minimum determinant
	 */
	public float getMinDeterminant() {
		return min_determinant;
	}

	/**
	 * Set the minimum determinant
	 * @param min_determinant
	 */
	public void setMinDeterminant(float min_determinant) {
		this.min_determinant = min_determinant;
	}

	/**
	 * @return the minimum displacement
	 */
	public float getMinDisplacement() {
		return min_displacement;
	}

	/**
	 * Set the minimum displacement
	 * @param min_displacement
	 */
	public void setMinDisplacement(float min_displacement) {
		this.min_displacement = min_displacement;
	}

	/**
	 * @return the maximum number of iterations
	 */
	public int getMaxIterations() {
		return max_iterations;
	}

	/**
	 * Set the maximum number of iterations
	 * @param max_iterations
	 */
	public void setMaxIterations(int max_iterations) {
		this.max_iterations = max_iterations;
	}

	/**
	 * @return the maximum residue
	 */
	public float getMaxResidue() {
		return max_residue;
	}

	/**
	 * Set the maximum residue
	 * @param max_residue 
	 */
	public void setMaxResidue(float max_residue) {
		this.max_residue = max_residue;
	}

	/**
	 * @return the step factor
	 */
	public float getStepFactor() {
		return step_factor;
	}

	/**
	 * Set the step factor
	 * @param step_factor
	 */
	public void setStepFactor(float step_factor) {
		this.step_factor = step_factor;
	}

	/**
	 * @return the amount of subsampling
	 */
	public int getSubsampling() {
		return subsampling;
	}

	/**
	 * Set the amount of subsampling
	 * @param subsampling
	 */
	public void setSubsampling(int subsampling) {
		this.subsampling = subsampling;
	}

	/**
	 * @return true if the affine consistency check is enabled; false otherwise.
	 */
	public int getAffineConsistencyCheck() {
		return affineConsistencyCheck;
	}

	/**
	 * Enable or disable the affine consistency check
	 * @param affineConsistencyCheck
	 */
	public void setAffineConsistencyCheck(int affineConsistencyCheck) {
		this.affineConsistencyCheck = affineConsistencyCheck;
	}

	/**
	 * Set the target
	 * @param targetArea
	 */
	public void setTargetArea(Shape targetArea) {
		this.targetArea = targetArea;
	}

	/**
	 * @return the target area
	 */
	public Shape getTargetArea() {
		return targetArea;
	}

	/**
	 * @param pyr set the previous pyramids
	 */
	public void setPreviousPyramid(PyramidSet pyr) {
		this.pyramid_last = pyr.imgPyr;
		this.pyramid_last_gradx = pyr.gradx;
		this.pyramid_last_grady = pyr.grady;
	}

	/**
	 * @return the previous pyramid
	 */
	public PyramidSet getPreviousPyramid() {
		PyramidSet ret = new PyramidSet();
		ret.imgPyr = this.pyramid_last;
		ret.gradx = this.pyramid_last_gradx;
		ret.grady = this.pyramid_last_grady;
		return ret;
	}
}
