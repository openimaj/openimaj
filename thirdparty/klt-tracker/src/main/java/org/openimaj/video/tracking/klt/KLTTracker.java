/**
 * This source code file is part of a direct port of Stan Birchfield's implementation
 * of a Kanade-Lucas-Tomasi feature tracker. The original implementation can be found
 * here: http://www.ces.clemson.edu/~stb/klt/
 *
 * As per the original code, the source code is in the public domain, available
 * for both commercial and non-commercial use.
 */
package org.openimaj.video.tracking.klt;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

/**
 * The KLT tracker
 * 
 * @author Stan Birchfield
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class KLTTracker {
	protected int KLT_verbose = 0;

	/**
	 * The feature was tracked
	 */
	public static final int KLT_TRACKED = 0;
	/**
	 * The feature was not found
	 */
	public static final int KLT_NOT_FOUND = -1;
	/**
	 * The determinant was too small
	 */
	public static final int KLT_SMALL_DET = -2;
	/**
	 * The maximum number of iterations was exceeded
	 */
	public static final int KLT_MAX_ITERATIONS = -3;
	/**
	 * The feature was out of bouns
	 */
	public static final int KLT_OOB = -4;
	/**
	 * The residue was too large
	 */
	public static final int KLT_LARGE_RESIDUE = -5;

	/**
	 * Modes of operation for selecting features.
	 */
	public enum SelectionMode {
		/**
		 * selecting all features
		 */
		SELECTING_ALL,
		/**
		 * Replacing some features
		 */
		REPLACING_SOME
	}

	TrackingContext tc;
	FeatureList featurelist;
	boolean isNorm = true; // true if input images are in [0..1] range, false if
							// [0..255]

	/**
	 * Construct with the given target number of features.
	 * 
	 * @param nfeatures
	 */
	public KLTTracker(int nfeatures) {
		this.tc = new TrackingContext();
		this.featurelist = new FeatureList(nfeatures);
	}

	/**
	 * Construct with the given context and feature list
	 * 
	 * @param tc
	 * @param featurelist
	 */
	public KLTTracker(TrackingContext tc, FeatureList featurelist) {
		this.tc = tc;
		this.featurelist = featurelist;
	}

	/*********************************************************************/
	private void _fillFeaturemap(int x, int y, BitSet featuremap, int mindist, int ncols, int nrows)
	{
		int ix, iy;

		for (iy = y - mindist; iy <= y + mindist; iy++)
			for (ix = x - mindist; ix <= x + mindist; ix++)
				if (ix >= 0 && ix < ncols && iy >= 0 && iy < nrows)
					featuremap.set(iy * ncols + ix);
	}

	/*********************************************************************
	 * _enforceMinimumDistance
	 * 
	 * Removes features that are within close proximity to better features.
	 * 
	 * INPUTS featurelist: A list of features. The nFeatures property is used.
	 * 
	 * OUTPUTS featurelist: Is overwritten. Nearby "redundant" features are
	 * removed. Writes -1's into the remaining elements.
	 * 
	 * RETURNS The number of remaining features.
	 */
	private void _enforceMinimumDistance(
			int[][] pointlist, /* featurepoints */
			int ncols, int nrows, /* size of images */
			int mindist, /* min. dist b/w features */
			int min_eigenvalue, /* min. eigenvalue */
			boolean overwriteAllFeatures)
	{
		final int npoints = pointlist.length;

		int indx; /* Index into features */
		int x, y, val; /*
						 * Location and trackability of pixel under
						 * consideration
						 */
		BitSet featuremap; /* Boolean array recording proximity of features */
		int ptr;

		/* Cannot add features with an eigenvalue less than one */
		if (min_eigenvalue < 1)
			min_eigenvalue = 1;

		/* Allocate memory for feature map and clear it */
		featuremap = new BitSet(ncols * nrows);

		/* Necessary because code below works with (mindist-1) */
		mindist--;

		/*
		 * If we are keeping all old good features, then add them to the
		 * featuremap
		 */
		if (!overwriteAllFeatures)
			for (indx = 0; indx < featurelist.features.length; indx++)
				if (featurelist.features[indx].val >= 0) {
					x = (int) featurelist.features[indx].x;
					y = (int) featurelist.features[indx].y;
					_fillFeaturemap(x, y, featuremap, mindist, ncols, nrows);
				}

		/* For each feature point, in descending order of importance, do ... */
		ptr = 0;
		indx = 0;
		while (true) {
			/*
			 * If we can't add all the points, then fill in the rest of the
			 * featurelist with -1's
			 */
			if (ptr >= npoints) {
				while (indx < featurelist.features.length) {
					if (overwriteAllFeatures ||
							featurelist.features[indx].val < 0)
					{
						featurelist.features[indx].x = -1;
						featurelist.features[indx].y = -1;
						featurelist.features[indx].val = KLT_NOT_FOUND;
						// featurelist.features[indx].aff_img = null;
						// featurelist.features[indx].aff_img_gradx = null;
						// featurelist.features[indx].aff_img_grady = null;
						// featurelist.features[indx].aff_x = -1.0f;
						// featurelist.features[indx].aff_y = -1.0f;
						// featurelist.features[indx].aff_Axx = 1.0f;
						// featurelist.features[indx].aff_Ayx = 0.0f;
						// featurelist.features[indx].aff_Axy = 0.0f;
						// featurelist.features[indx].aff_Ayy = 1.0f;
					}
					indx++;
				}
				break;
			}

			x = pointlist[ptr][0];
			y = pointlist[ptr][1];
			val = pointlist[ptr][2];
			ptr++;

			/* Ensure that feature is in-bounds */
			assert (x >= 0);
			assert (x < ncols);
			assert (y >= 0);
			assert (y < nrows);

			while (!overwriteAllFeatures &&
					indx < featurelist.features.length &&
					featurelist.features[indx].val >= 0)
				indx++;

			if (indx >= featurelist.features.length)
				break;

			/*
			 * If no neighbor has been selected, and if the minimum eigenvalue
			 * is large enough, then add feature to the current list
			 */
			if (!featuremap.get(y * ncols + x) && val >= min_eigenvalue) {
				featurelist.features[indx].x = x;
				featurelist.features[indx].y = y;
				featurelist.features[indx].val = val;
				// featurelist.features[indx].aff_img = null;
				// featurelist.features[indx].aff_img_gradx = null;
				// featurelist.features[indx].aff_img_grady = null;
				// featurelist.features[indx].aff_x = -1.0f;
				// featurelist.features[indx].aff_y = -1.0f;
				// featurelist.features[indx].aff_Axx = 1.0f;
				// featurelist.features[indx].aff_Ayx = 0.0f;
				// featurelist.features[indx].aff_Axy = 0.0f;
				// featurelist.features[indx].aff_Ayy = 1.0f;
				indx++;

				/*
				 * Fill in surrounding region of feature map, but make sure that
				 * pixels are in-bounds
				 */
				_fillFeaturemap(x, y, featuremap, mindist, ncols, nrows);
			}
		}
	}

	/*********************************************************************
	 * _sortPointList
	 */
	private void _sortPointList(int[][] pointlist)
	{
		Arrays.sort(pointlist, new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				final int v1 = o1[2];
				final int v2 = o2[2];

				if (v1 > v2)
					return (-1);
				else if (v1 < v2)
					return (1);
				else
					return (0);
			}
		});
	}

	/*********************************************************************
	 * _minEigenvalue
	 * 
	 * Given the three distinct elements of the symmetric 2x2 matrix [gxx gxy]
	 * [gxy gyy], Returns the minimum eigenvalue of the matrix.
	 */
	private float _minEigenvalue(float gxx, float gxy, float gyy)
	{
		return (float) ((gxx + gyy - Math.sqrt((gxx - gyy) * (gxx - gyy) + 4 * gxy * gxy)) / 2.0f);
	}

	/**
	 * @throws IOException
	 *******************************************************************/
	private void _selectGoodFeatures(
			FImage img,
			SelectionMode mode)
	{
		final int nrows = img.height, ncols = img.width;
		FImage floatimg, gradx, grady;
		int window_hw, window_hh;
		int[][] pointlist;

		final boolean overwriteAllFeatures = (mode == SelectionMode.SELECTING_ALL) ? true : false;
		// boolean floatimages_created = false;

		/* Check window size (and correct if necessary) */
		if (tc.window_width % 2 != 1) {
			tc.window_width = tc.window_width + 1;
			System.err.format("Tracking context's window width must be odd.  Changing to %d.\n", tc.window_width);
		}
		if (tc.window_height % 2 != 1) {
			tc.window_height = tc.window_height + 1;
			System.err.format("Tracking context's window height must be odd.  Changing to %d.\n", tc.window_height);
		}
		if (tc.window_width < 3) {
			tc.window_width = 3;
			System.err.format("Tracking context's window width must be at least three.  \nChanging to %d.\n",
					tc.window_width);
		}
		if (tc.window_height < 3) {
			tc.window_height = 3;
			System.err.format("Tracking context's window height must be at least three.  \nChanging to %d.\n",
					tc.window_height);
		}
		window_hw = tc.window_width / 2;
		window_hh = tc.window_height / 2;

		/* Create pointlist, which is a simplified version of a featurelist, */
		/* for speed. Contains only integer locations and values. */
		pointlist = new int[ncols * nrows][3];

		/* Create temporary images, etc. */
		final PyramidSet ppSet = tc.previousPyramidSet();
		if (mode == SelectionMode.REPLACING_SOME &&
				tc.sequentialMode && ppSet != null)
		{
			floatimg = ppSet.imgPyr.img[0];
			gradx = ppSet.gradx.img[0];
			grady = ppSet.grady.img[0];
			assert (gradx != null);
			assert (grady != null);
		} else {
			// floatimages_created = true;
			floatimg = new FImage(ncols, nrows);
			gradx = new FImage(ncols, nrows);
			grady = new FImage(ncols, nrows);
			if (tc.smoothBeforeSelecting) {
				floatimg = img.process(new FGaussianConvolve(tc.computeSmoothSigma()));
			} else {
				floatimg = img.clone();
			}

			/* Compute gradient of image in x and y direction */
			tc.computeGradients(floatimg, tc.grad_sigma, gradx, grady);
		}

		/* Write internal images */
		if (tc.writeInternalImages) {
			try {
				ImageUtilities.write(floatimg, "png", new File("kltimg_sgfrlf.png"));
				ImageUtilities.write(gradx, "png", new File("kltimg_sgfrlf_gx.png"));
				ImageUtilities.write(grady, "png", new File("kltimg_sgfrlf_gy.png"));
			} catch (final IOException e) {

			}
		}

		/*
		 * Compute trackability of each image pixel as the minimum of the two
		 * eigenvalues of the Z matrix
		 */
		{
			float gx, gy;
			float gxx, gxy, gyy;
			int xx, yy;
			int ptr;
			float val;
			int limit = 1;
			int borderx = tc.borderx; /* Must not touch cols */
			int bordery = tc.bordery; /* lost by convolution */
			int x, y;

			if (borderx < window_hw)
				borderx = window_hw;
			if (bordery < window_hh)
				bordery = window_hh;

			/* Find largest value of an int */
			limit = Integer.MAX_VALUE / 2 - 1;

			/* For most of the pixels in the image, do ... */
			ptr = 0;
			for (y = bordery; y < nrows - bordery; y += tc.nSkippedPixels + 1)
				for (x = borderx; x < ncols - borderx; x += tc.nSkippedPixels + 1) {
					if (tc.getTargetArea() != null) {
						final Point2d point = new Point2dImpl(x, y);
						if (!tc.getTargetArea().isInside(point))
							continue;
					}
					/* Sum the gradients in the surrounding window */
					gxx = 0;
					gxy = 0;
					gyy = 0;
					for (yy = y - window_hh; yy <= y + window_hh; yy++)
						for (xx = x - window_hw; xx <= x + window_hw; xx++) {
							gx = gradx.pixels[yy][xx];
							gy = grady.pixels[yy][xx];
							gxx += gx * gx;
							gxy += gx * gy;
							gyy += gy * gy;
						}

					/*
					 * Store the trackability of the pixel as the minimum of the
					 * two eigenvalues
					 */
					pointlist[ptr][0] = x;
					pointlist[ptr][1] = y;
					val = _minEigenvalue(gxx, gxy, gyy);
					if (val > limit) {
						System.err
								.format("(_KLTSelectGoodFeatures) minimum eigenvalue %f is greater than the capacity of an int; setting to maximum value",
										val);
						val = limit;
					}
					pointlist[ptr][2] = (int) val;
					++ptr;
				}
		}

		/* Sort the features */
		_sortPointList(pointlist);

		/* Check tc.mindist */
		if (tc.mindist < 0) {
			System.err.format(
					"(_KLTSelectGoodFeatures) Tracking context field tc.mindist is negative (%d); setting to zero",
					tc.mindist);
			tc.mindist = 0;
		}

		/* Enforce minimum distance between features */
		_enforceMinimumDistance(
				pointlist,
				ncols, nrows,
				tc.mindist,
				tc.min_eigenvalue,
				overwriteAllFeatures);
	}

	/*********************************************************************
	 * KLTSelectGoodFeatures
	 * 
	 * Main routine, visible to the outside. Finds the good features in an
	 * image.
	 * 
	 * INPUTS tc: Contains parameters used in computation (size of image, size
	 * of window, min distance b/w features, sigma to compute image gradients, #
	 * of features desired).
	 * 
	 * @param img
	 *            Pointer to the data of an image (probably unsigned chars).
	 * 
	 *            OUTPUTS features: List of features. The member nFeatures is
	 *            computed.
	 */
	public void selectGoodFeatures(FImage img) {
		if (isNorm)
			img = img.multiply(255f);

		if (KLT_verbose >= 1) {
			System.err.format("(KLT) Selecting the %d best features from a %d by %d image...  ",
					featurelist.features.length, img.width, img.height);
		}

		_selectGoodFeatures(img, SelectionMode.SELECTING_ALL);

		if (KLT_verbose >= 1) {
			System.err.format("\n\t%d features found.\n", featurelist.countRemainingFeatures());
			if (tc.writeInternalImages)
				System.err.format("\tWrote images to 'kltimg_sgfrlf*.pgm'.\n");
		}
	}

	/*********************************************************************
	 * KLTReplaceLostFeatures
	 * 
	 * Main routine, visible to the outside. Replaces the lost features in an
	 * image.
	 * 
	 * INPUTS tc: Contains parameters used in computation (size of image, size
	 * of window, min distance b/w features, sigma to compute image gradients, #
	 * of features desired).
	 * 
	 * @param img
	 *            Pointer to the data of an image (probably unsigned chars).
	 * 
	 *            OUTPUTS features: List of features. The member nFeatures is
	 *            computed.
	 */
	public void replaceLostFeatures(FImage img)
	{
		if (isNorm)
			img = img.multiply(255f);

		final int nLostFeatures = featurelist.features.length - featurelist.countRemainingFeatures();

		if (KLT_verbose >= 1) {
			System.err.format("(KLT) Attempting to replace %d features in a %d by %d image...  ", nLostFeatures,
					img.width, img.height);
		}

		/* If there are any lost features, replace them */
		if (nLostFeatures > 0)
			_selectGoodFeatures(img, SelectionMode.REPLACING_SOME);

		if (KLT_verbose >= 1) {
			System.err.format("\n\t%d features replaced.\n",
					nLostFeatures - featurelist.features.length + featurelist.countRemainingFeatures());
			if (tc.writeInternalImages)
				System.err.format("\tWrote images to 'kltimg_sgfrlf*.pgm'.\n");
		}
	}

	/*********************************************************************
	 * KLTSetVerbosity
	 * 
	 * @param verbosity
	 */
	public void setVerbosity(int verbosity)
	{
		KLT_verbose = verbosity;
	}

	/*********************************************************************
	 * _interpolate
	 * 
	 * Given a point (x,y) in an image, computes the bilinear interpolated
	 * gray-level value of the point in the image.
	 */
	private float _interpolate(float x, float y, FImage img)
	{
		final int xt = (int) x; /* coordinates of top-left corner */
		final int yt = (int) y;
		final float ax = x - xt;
		final float ay = y - yt;

		final float x0y0 = img.pixels[yt][xt];
		final float x1y0 = img.pixels[yt][xt + 1];
		final float x0y1 = img.pixels[yt + 1][xt];
		final float x1y1 = img.pixels[yt + 1][xt + 1];

		return ((1 - ax) * (1 - ay) * x0y0 +
				ax * (1 - ay) * x1y0 +
				(1 - ax) * ay * x0y1 + ax * ay * x1y1);
	}

	/*********************************************************************
	 * _computeIntensityDifference
	 * 
	 * Given two images and the window center in both images, aligns the images
	 * wrt the window and computes the difference between the two overlaid
	 * images.
	 */
	private void _computeIntensityDifference(
			FImage img1, /* images */
			FImage img2,
			float x1, float y1, /* center of window in 1st img */
			float x2, float y2, /* center of window in 2nd img */
			int width, int height, /* size of window */
			float[] imgdiff) /* output */
	{
		final int hw = width / 2, hh = height / 2;
		float g1, g2;
		int i, j;

		/* Compute values */
		int idx = 0;
		for (j = -hh; j <= hh; j++)
			for (i = -hw; i <= hw; i++) {
				g1 = _interpolate(x1 + i, y1 + j, img1);
				g2 = _interpolate(x2 + i, y2 + j, img2);
				imgdiff[idx++] = g1 - g2;
			}
	}

	/*********************************************************************
	 * _computeGradientSum
	 * 
	 * Given two gradients and the window center in both images, aligns the
	 * gradients wrt the window and computes the sum of the two overlaid
	 * gradients.
	 */
	private void _computeGradientSum(
			FImage gradx1, /* gradient images */
			FImage grady1,
			FImage gradx2,
			FImage grady2,
			float x1, float y1, /* center of window in 1st img */
			float x2, float y2, /* center of window in 2nd img */
			int width, int height, /* size of window */
			float[] gradx, /* output */
			float[] grady) /* " */
	{
		final int hw = width / 2, hh = height / 2;
		float g1, g2;
		int i, j;

		int gx = 0, gy = 0;
		/* Compute values */
		for (j = -hh; j <= hh; j++)
			for (i = -hw; i <= hw; i++) {
				g1 = _interpolate(x1 + i, y1 + j, gradx1);
				g2 = _interpolate(x2 + i, y2 + j, gradx2);
				// *gradx++ = g1 + g2;
				gradx[gx++] = g1 + g2;
				g1 = _interpolate(x1 + i, y1 + j, grady1);
				g2 = _interpolate(x2 + i, y2 + j, grady2);
				// *grady++ = g1 + g2;
				grady[gy++] = g1 + g2;
			}
	}

	/*********************************************************************
	 * _computeIntensityDifferenceLightingInsensitive
	 * 
	 * Given two images and the window center in both images, aligns the images
	 * wrt the window and computes the difference between the two overlaid
	 * images; normalizes for overall gain and bias.
	 */
	private void _computeIntensityDifferenceLightingInsensitive(
			FImage img1, /* images */
			FImage img2,
			float x1, float y1, /* center of window in 1st img */
			float x2, float y2, /* center of window in 2nd img */
			int width, int height, /* size of window */
			float[] imgdiff) /* output */
	{
		final int hw = width / 2, hh = height / 2;
		float g1, g2, sum1_squared = 0, sum2_squared = 0;
		int i, j;

		float sum1 = 0, sum2 = 0;
		float mean1, mean2, alpha, belta;
		/* Compute values */
		for (j = -hh; j <= hh; j++)
			for (i = -hw; i <= hw; i++) {
				g1 = _interpolate(x1 + i, y1 + j, img1);
				g2 = _interpolate(x2 + i, y2 + j, img2);
				sum1 += g1;
				sum2 += g2;
				sum1_squared += g1 * g1;
				sum2_squared += g2 * g2;
			}
		mean1 = sum1_squared / (width * height);
		mean2 = sum2_squared / (width * height);
		alpha = (float) Math.sqrt(mean1 / mean2);
		mean1 = sum1 / (width * height);
		mean2 = sum2 / (width * height);
		belta = mean1 - alpha * mean2;

		int id = 0;
		for (j = -hh; j <= hh; j++)
			for (i = -hw; i <= hw; i++) {
				g1 = _interpolate(x1 + i, y1 + j, img1);
				g2 = _interpolate(x2 + i, y2 + j, img2);
				// *imgdiff++ = g1- g2*alpha-belta;
				imgdiff[id++] = g1 - g2 * alpha - belta;
			}
	}

	/*********************************************************************
	 * _computeGradientSumLightingInsensitive
	 * 
	 * Given two gradients and the window center in both images, aligns the
	 * gradients wrt the window and computes the sum of the two overlaid
	 * gradients; normalizes for overall gain and bias.
	 */
	private void _computeGradientSumLightingInsensitive(
			FImage gradx1, /* gradient images */
			FImage grady1,
			FImage gradx2,
			FImage grady2,
			FImage img1, /* images */
			FImage img2,

			float x1, float y1, /* center of window in 1st img */
			float x2, float y2, /* center of window in 2nd img */
			int width, int height, /* size of window */
			float[] gradx, /* output */
			float[] grady) /* " */
	{
		final int hw = width / 2, hh = height / 2;
		float g1, g2, sum1_squared = 0, sum2_squared = 0;
		int i, j;

		// float sum1 = 0, sum2 = 0;
		float mean1, mean2, alpha;
		for (j = -hh; j <= hh; j++)
			for (i = -hw; i <= hw; i++) {
				g1 = _interpolate(x1 + i, y1 + j, img1);
				g2 = _interpolate(x2 + i, y2 + j, img2);
				sum1_squared += g1;
				sum2_squared += g2;
			}
		mean1 = sum1_squared / (width * height);
		mean2 = sum2_squared / (width * height);
		alpha = (float) Math.sqrt(mean1 / mean2);

		int gx = 0, gy = 0;
		/* Compute values */
		for (j = -hh; j <= hh; j++)
			for (i = -hw; i <= hw; i++) {
				g1 = _interpolate(x1 + i, y1 + j, gradx1);
				g2 = _interpolate(x2 + i, y2 + j, gradx2);
				// *gradx++ = g1 + g2*alpha;
				gradx[gx++] = g1 + g2 * alpha;
				g1 = _interpolate(x1 + i, y1 + j, grady1);
				g2 = _interpolate(x2 + i, y2 + j, grady2);
				// *grady++ = g1+ g2*alpha;
				grady[gy++] = g1 + g2 * alpha;
			}
	}

	/*********************************************************************
	 * _compute2by2GradientMatrix
	 * 
	 */
	private float[] _compute2by2GradientMatrix(
			float[] gradx,
			float[] grady,
			int width, /* size of window */
			int height
			)

	{
		float gx, gy;
		int i;

		float gxx; /* return values */
		float gxy;
		float gyy;

		/* Compute values */
		gxx = 0.0f;
		gxy = 0.0f;
		gyy = 0.0f;
		for (i = 0; i < width * height; i++) {
			// gx = *gradx++;
			gx = gradx[i];
			// gy = *grady++;
			gy = grady[i];
			gxx += gx * gx;
			gxy += gx * gy;
			gyy += gy * gy;
		}

		return new float[] { gxx, gxy, gyy };
	}

	/*********************************************************************
	 * _compute2by1ErrorVector
	 * 
	 */

	private float[] _compute2by1ErrorVector(
			float[] imgdiff,
			float[] gradx,
			float[] grady,
			int width, /* size of window */
			int height,
			float step_factor /*
							 * 2.0 comes from equations, 1.0 seems to avoid
							 * overshooting
							 */
			)
	{
		float diff;
		int i;

		float ex, ey;

		/* Compute values */
		ex = 0;
		ey = 0;
		for (i = 0; i < width * height; i++) {
			diff = imgdiff[i];
			ex += diff * gradx[i];
			ey += diff * grady[i];
		}
		ex *= step_factor;
		ey *= step_factor;

		return new float[] { ex, ey };
	}

	/*********************************************************************
	 * _solveEquation
	 * 
	 * Solves the 2x2 matrix equation [gxx gxy] [dx] = [ex] [gxy gyy] [dy] =
	 * [ey] for dx and dy.
	 * 
	 * Returns KLT_TRACKED on success and KLT_SMALL_DET on failure
	 */
	private int _solveEquation(
			float gxx, float gxy, float gyy,
			float ex, float ey,
			float small,
			float[] output)
	{
		final float det = gxx * gyy - gxy * gxy;

		if (det < small)
			return KLT_SMALL_DET;

		output[0] = (gyy * ex - gxy * ey) / det; // dx
		output[1] = (gxx * ey - gxy * ex) / det; // dy
		return KLT_TRACKED;
	}

	/*********************************************************************
	 * _sumAbsFloatWindow
	 */
	private float _sumAbsFloatWindow(
			float[] fw,
			int width,
			int height)
	{
		float sum = 0.0f;

		for (int i = 0; i < height * width; i++)
			sum += Math.abs(fw[i]);

		return sum;
	}

	/*********************************************************************
	 * _trackFeature
	 * 
	 * Tracks a feature point from one image to the next.
	 * 
	 * RETURNS KLT_SMALL_DET if feature is lost, KLT_MAX_ITERATIONS if tracking
	 * stopped because iterations timed out, KLT_TRACKED otherwise.
	 */
	private int _trackFeature(
			float x1, /* location of window in first image */
			float y1,
			float[] xy2, /* starting location of search in second image */
			FImage img1,
			FImage gradx1,
			FImage grady1,
			FImage img2,
			FImage gradx2,
			FImage grady2,
			int width, /* size of window */
			int height,
			float step_factor, /*
								 * 2.0 comes from equations, 1.0 seems to avoid
								 * overshooting
								 */
			int max_iterations,
			float small, /* determinant threshold for declaring KLT_SMALL_DET */
			float th, /* displacement threshold for stopping */
			float max_residue, /*
								 * residue threshold for declaring
								 * KLT_LARGE_RESIDUE
								 */
			boolean lighting_insensitive) /*
										 * whether to normalize for gain and
										 * bias
										 */
	{
		float[] imgdiff, gradx, grady;
		float gxx, gxy, gyy, ex, ey, dx, dy;
		int iteration = 0;
		int status;
		final int hw = width / 2;
		final int hh = height / 2;
		final int nc = img1.width;
		final int nr = img1.height;
		final float one_plus_eps = 1.001f; /* To prevent rounding errors */

		/* Allocate memory for windows */
		imgdiff = new float[height * width];
		gradx = new float[height * width];
		grady = new float[height * width];

		/* Iteratively update the window position */
		do {

			/* If out of bounds, exit loop */
			if (x1 - hw < 0.0f || nc - (x1 + hw) < one_plus_eps ||
					xy2[0] - hw < 0.0f || nc - (xy2[0] + hw) < one_plus_eps ||
					y1 - hh < 0.0f || nr - (y1 + hh) < one_plus_eps ||
					xy2[1] - hh < 0.0f || nr - (xy2[1] + hh) < one_plus_eps)
			{
				status = KLT_OOB;
				break;
			}

			/* Compute gradient and difference windows */
			if (lighting_insensitive) {
				_computeIntensityDifferenceLightingInsensitive(img1, img2, x1, y1, xy2[0], xy2[1], width, height, imgdiff);
				_computeGradientSumLightingInsensitive(gradx1, grady1, gradx2, grady2, img1, img2, x1, y1, xy2[0],
						xy2[1], width, height, gradx, grady);
			} else {
				_computeIntensityDifference(img1, img2, x1, y1, xy2[0], xy2[1], width, height, imgdiff);
				_computeGradientSum(gradx1, grady1, gradx2, grady2, x1, y1, xy2[0], xy2[1], width, height, gradx, grady);
			}

			/* Use these windows to construct matrices */
			float[] tmp = _compute2by2GradientMatrix(gradx, grady, width, height);
			gxx = tmp[0];
			gxy = tmp[1];
			gyy = tmp[2];

			tmp = _compute2by1ErrorVector(imgdiff, gradx, grady, width, height, step_factor);
			ex = tmp[0];
			ey = tmp[1];

			/* Using matrices, solve equation for new displacement */
			tmp = new float[2];
			status = _solveEquation(gxx, gxy, gyy, ex, ey, small, tmp);
			dx = tmp[0];
			dy = tmp[1];
			if (status == KLT_SMALL_DET)
				break;

			xy2[0] += dx;
			xy2[1] += dy;
			iteration++;

		} while ((Math.abs(dx) >= th || Math.abs(dy) >= th) && iteration < max_iterations);

		/* Check whether window is out of bounds */
		if (xy2[0] - hw < 0.0f || nc - (xy2[0] + hw) < one_plus_eps ||
				xy2[1] - hh < 0.0f || nr - (xy2[1] + hh) < one_plus_eps)
			status = KLT_OOB;

		/* Check whether residue is too large */
		if (status == KLT_TRACKED) {
			if (lighting_insensitive)
				_computeIntensityDifferenceLightingInsensitive(img1, img2, x1, y1, xy2[0], xy2[1], width, height, imgdiff);
			else
				_computeIntensityDifference(img1, img2, x1, y1, xy2[0], xy2[1], width, height, imgdiff);
			if (_sumAbsFloatWindow(imgdiff, width, height) / (width * height) > max_residue)
				status = KLT_LARGE_RESIDUE;
		}

		/* Return appropriate value */
		if (status == KLT_SMALL_DET)
			return KLT_SMALL_DET;
		else if (status == KLT_OOB)
			return KLT_OOB;
		else if (status == KLT_LARGE_RESIDUE)
			return KLT_LARGE_RESIDUE;
		else if (iteration >= max_iterations)
			return KLT_MAX_ITERATIONS;
		else
			return KLT_TRACKED;
	}

	/*********************************************************************/

	private boolean _outOfBounds(
			float x,
			float y,
			int ncols,
			int nrows,
			int borderx,
			int bordery)
	{
		return (x < borderx || x > ncols - 1 - borderx ||
				y < bordery || y > nrows - 1 - bordery);
	}

	// /**********************************************************************
	// * CONSISTENCY CHECK OF FEATURES BY AFFINE MAPPING (BEGIN)
	// *
	// * Created by: Thorsten Thormaehlen (University of Hannover) June 2004
	// * thormae@tnt.uni-hannover.de
	// *
	// * Permission is granted to any individual or institution to use, copy,
	// modify,
	// * and distribute this part of the software, provided that this complete
	// authorship
	// * and permission notice is maintained, intact, in all copies.
	// *
	// * This software is provided "as is" without express or implied warranty.
	// *
	// *
	// * The following static functions are helpers for the affine mapping.
	// * They all start with "_am".
	// * There are also small changes in other files for the
	// * affine mapping these are all marked by "for affine mapping"
	// *
	// * Thanks to Kevin Koeser (koeser@mip.informatik.uni-kiel.de) for fixing a
	// bug
	// */
	//
	// #define SWAP_ME(X,Y) {temp=(X);(X)=(Y);(Y)=temp;}
	//
	// static float **_am_matrix(long nr, long nc)
	// {
	// float **m;
	// int a;
	// m = (float **) malloc((size_t)(nr*sizeof(float*)));
	// m[0] = (float *) malloc((size_t)((nr*nc)*sizeof(float)));
	// for(a = 1; a < nr; a++) m[a] = m[a-1]+nc;
	// return m;
	// }
	//
	// static void _am_free_matrix(float **m)
	// {
	// free(m[0]);
	// free(m);
	// }
	//
	//
	// static int _am_gauss_jordan_elimination(float **a, int n, float **b, int
	// m)
	// {
	// /* re-implemented from Numerical Recipes in C */
	// int *indxc,*indxr,*ipiv;
	// int i,j,k,l,ll;
	// float big,dum,pivinv,temp;
	// int col = 0;
	// int row = 0;
	//
	// indxc=(int *)malloc((size_t) (n*sizeof(int)));
	// indxr=(int *)malloc((size_t) (n*sizeof(int)));
	// ipiv=(int *)malloc((size_t) (n*sizeof(int)));
	// for (j=0;j<n;j++) ipiv[j]=0;
	// for (i=0;i<n;i++) {
	// big=0.0;
	// for (j=0;j<n;j++)
	// if (ipiv[j] != 1)
	// for (k=0;k<n;k++) {
	// if (ipiv[k] == 0) {
	// if (fabs(a[j][k]) >= big) {
	// big= (float) fabs(a[j][k]);
	// row=j;
	// col=k;
	// }
	// } else if (ipiv[k] > 1) return KLT_SMALL_DET;
	// }
	// ++(ipiv[col]);
	// if (row != col) {
	// for (l=0;l<n;l++) SWAP_ME(a[row][l],a[col][l])
	// for (l=0;l<m;l++) SWAP_ME(b[row][l],b[col][l])
	// }
	// indxr[i]=row;
	// indxc[i]=col;
	// if (a[col][col] == 0.0) return KLT_SMALL_DET;
	// pivinv=1.0f/a[col][col];
	// a[col][col]=1.0;
	// for (l=0;l<n;l++) a[col][l] *= pivinv;
	// for (l=0;l<m;l++) b[col][l] *= pivinv;
	// for (ll=0;ll<n;ll++)
	// if (ll != col) {
	// dum=a[ll][col];
	// a[ll][col]=0.0;
	// for (l=0;l<n;l++) a[ll][l] -= a[col][l]*dum;
	// for (l=0;l<m;l++) b[ll][l] -= b[col][l]*dum;
	// }
	// }
	// for (l=n-1;l>=0;l--) {
	// if (indxr[l] != indxc[l])
	// for (k=0;k<n;k++)
	// SWAP_ME(a[k][indxr[l]],a[k][indxc[l]]);
	// }
	// free(ipiv);
	// free(indxr);
	// free(indxc);
	//
	// return KLT_TRACKED;
	// }
	//
	// /*********************************************************************
	// * _am_getGradientWinAffine
	// *
	// * aligns the gradients with the affine transformed window
	// */
	//
	// static void _am_getGradientWinAffine(
	// FImage in_gradx,
	// FImage in_grady,
	// float x, float y, /* center of window*/
	// float Axx, float Ayx , float Axy, float Ayy, /* affine mapping */
	// int width, int height, /* size of window */
	// _FloatWindow out_gradx, /* output */
	// _FloatWindow out_grady) /* output */
	// {
	// int hw = width/2, hh = height/2;
	// int i, j;
	// float mi, mj;
	//
	// /* Compute values */
	// for (j = -hh ; j <= hh ; j++)
	// for (i = -hw ; i <= hw ; i++) {
	// mi = Axx * i + Axy * j;
	// mj = Ayx * i + Ayy * j;
	// *out_gradx++ = _interpolate(x+mi, y+mj, in_gradx);
	// *out_grady++ = _interpolate(x+mi, y+mj, in_grady);
	// }
	//
	// }
	//
	// /*********************************************************************
	// * _computeAffineMappedImage
	// * used only for DEBUG output
	// *
	// */
	//
	// static void _am_computeAffineMappedImage(
	// FImage img, /* images */
	// float x, float y, /* center of window */
	// float Axx, float Ayx , float Axy, float Ayy, /* affine mapping */
	// int width, int height, /* size of window */
	// _FloatWindow imgdiff) /* output */
	// {
	// int hw = width/2, hh = height/2;
	// int i, j;
	// float mi, mj;
	//
	// /* Compute values */
	// for (j = -hh ; j <= hh ; j++)
	// for (i = -hw ; i <= hw ; i++) {
	// mi = Axx * i + Axy * j;
	// mj = Ayx * i + Ayy * j;
	// *imgdiff++ = _interpolate(x+mi, y+mj, img);
	// }
	// }
	//
	//
	// /*********************************************************************
	// * _getSubFloatImage
	// */
	//
	// static void _am_getSubFloatImage(
	// FImage img, /* image */
	// float x, float y, /* center of window */
	// FImage window) /* output */
	// {
	// int hw = window.ncols/2, hh = window.nrows/2;
	// int x0 = (int) x;
	// int y0 = (int) y;
	// float * windata = window.data;
	// int offset;
	// int i, j;
	//
	// assert(x0 - hw >= 0);
	// assert(y0 - hh >= 0);
	// assert(x0 + hw <= img.ncols);
	// assert(y0 + hh <= img.nrows);
	//
	// /* copy values */
	// for (j = -hh ; j <= hh ; j++)
	// for (i = -hw ; i <= hw ; i++) {
	// offset = (j+y0)*img.ncols + (i+x0);
	// *windata++ = *(img.data+offset);
	// }
	// }
	//
	// /*********************************************************************
	// * _am_computeIntensityDifferenceAffine
	// *
	// * Given two images and the window center in both images,
	// * aligns the images with the window and computes the difference
	// * between the two overlaid images using the affine mapping.
	// * A = [ Axx Axy]
	// * [ Ayx Ayy]
	// */
	//
	// static void _am_computeIntensityDifferenceAffine(
	// FImage img1, /* images */
	// FImage img2,
	// float x1, float y1, /* center of window in 1st img */
	// float x2, float y2, /* center of window in 2nd img */
	// float Axx, float Ayx , float Axy, float Ayy, /* affine mapping */
	// int width, int height, /* size of window */
	// _FloatWindow imgdiff) /* output */
	// {
	// int hw = width/2, hh = height/2;
	// float g1, g2;
	// int i, j;
	// float mi, mj;
	//
	// /* Compute values */
	// for (j = -hh ; j <= hh ; j++)
	// for (i = -hw ; i <= hw ; i++) {
	// g1 = _interpolate(x1+i, y1+j, img1);
	// mi = Axx * i + Axy * j;
	// mj = Ayx * i + Ayy * j;
	// g2 = _interpolate(x2+mi, y2+mj, img2);
	// *imgdiff++ = g1 - g2;
	// }
	// }
	//
	// /*********************************************************************
	// * _am_compute6by6GradientMatrix
	// *
	// */
	//
	// static void _am_compute6by6GradientMatrix(
	// _FloatWindow gradx,
	// _FloatWindow grady,
	// int width, /* size of window */
	// int height,
	// float **T) /* return values */
	// {
	// int hw = width/2, hh = height/2;
	// int i, j;
	// float gx, gy, gxx, gxy, gyy, x, y, xx, xy, yy;
	//
	//
	// /* Set values to zero */
	// for (j = 0 ; j < 6 ; j++) {
	// for (i = j ; i < 6 ; i++) {
	// T[j][i] = 0.0;
	// }
	// }
	//
	// for (j = -hh ; j <= hh ; j++) {
	// for (i = -hw ; i <= hw ; i++) {
	// gx = *gradx++;
	// gy = *grady++;
	// gxx = gx * gx;
	// gxy = gx * gy;
	// gyy = gy * gy;
	// x = (float) i;
	// y = (float) j;
	// xx = x * x;
	// xy = x * y;
	// yy = y * y;
	//
	// T[0][0] += xx * gxx;
	// T[0][1] += xx * gxy;
	// T[0][2] += xy * gxx;
	// T[0][3] += xy * gxy;
	// T[0][4] += x * gxx;
	// T[0][5] += x * gxy;
	//
	// T[1][1] += xx * gyy;
	// T[1][2] += xy * gxy;
	// T[1][3] += xy * gyy;
	// T[1][4] += x * gxy;
	// T[1][5] += x * gyy;
	//
	// T[2][2] += yy * gxx;
	// T[2][3] += yy * gxy;
	// T[2][4] += y * gxx;
	// T[2][5] += y * gxy;
	//
	// T[3][3] += yy * gyy;
	// T[3][4] += y * gxy;
	// T[3][5] += y * gyy;
	//
	// T[4][4] += gxx;
	// T[4][5] += gxy;
	//
	// T[5][5] += gyy;
	// }
	// }
	//
	// for (j = 0 ; j < 5 ; j++) {
	// for (i = j+1 ; i < 6 ; i++) {
	// T[i][j] = T[j][i];
	// }
	// }
	//
	// }
	//
	//
	//
	// /*********************************************************************
	// * _am_compute6by1ErrorVector
	// *
	// */
	//
	// static void _am_compute6by1ErrorVector(
	// _FloatWindow imgdiff,
	// _FloatWindow gradx,
	// _FloatWindow grady,
	// int width, /* size of window */
	// int height,
	// float **e) /* return values */
	// {
	// int hw = width/2, hh = height/2;
	// int i, j;
	// float diff, diffgradx, diffgrady;
	//
	// /* Set values to zero */
	// for(i = 0; i < 6; i++) e[i][0] = 0.0;
	//
	// /* Compute values */
	// for (j = -hh ; j <= hh ; j++) {
	// for (i = -hw ; i <= hw ; i++) {
	// diff = *imgdiff++;
	// diffgradx = diff * (*gradx++);
	// diffgrady = diff * (*grady++);
	// e[0][0] += diffgradx * i;
	// e[1][0] += diffgrady * i;
	// e[2][0] += diffgradx * j;
	// e[3][0] += diffgrady * j;
	// e[4][0] += diffgradx;
	// e[5][0] += diffgrady;
	// }
	// }
	//
	// for(i = 0; i < 6; i++) e[i][0] *= 0.5;
	//
	// }
	//
	//
	// /*********************************************************************
	// * _am_compute4by4GradientMatrix
	// *
	// */
	//
	// static void _am_compute4by4GradientMatrix(
	// _FloatWindow gradx,
	// _FloatWindow grady,
	// int width, /* size of window */
	// int height,
	// float **T) /* return values */
	// {
	// int hw = width/2, hh = height/2;
	// int i, j;
	// float gx, gy, x, y;
	//
	//
	// /* Set values to zero */
	// for (j = 0 ; j < 4 ; j++) {
	// for (i = 0 ; i < 4 ; i++) {
	// T[j][i] = 0.0;
	// }
	// }
	//
	// for (j = -hh ; j <= hh ; j++) {
	// for (i = -hw ; i <= hw ; i++) {
	// gx = *gradx++;
	// gy = *grady++;
	// x = (float) i;
	// y = (float) j;
	// T[0][0] += (x*gx+y*gy) * (x*gx+y*gy);
	// T[0][1] += (x*gx+y*gy)*(x*gy-y*gx);
	// T[0][2] += (x*gx+y*gy)*gx;
	// T[0][3] += (x*gx+y*gy)*gy;
	//
	// T[1][1] += (x*gy-y*gx) * (x*gy-y*gx);
	// T[1][2] += (x*gy-y*gx)*gx;
	// T[1][3] += (x*gy-y*gx)*gy;
	//
	// T[2][2] += gx*gx;
	// T[2][3] += gx*gy;
	//
	// T[3][3] += gy*gy;
	// }
	// }
	//
	// for (j = 0 ; j < 3 ; j++) {
	// for (i = j+1 ; i < 4 ; i++) {
	// T[i][j] = T[j][i];
	// }
	// }
	//
	// }
	//
	// /*********************************************************************
	// * _am_compute4by1ErrorVector
	// *
	// */
	//
	// static void _am_compute4by1ErrorVector(
	// _FloatWindow imgdiff,
	// _FloatWindow gradx,
	// _FloatWindow grady,
	// int width, /* size of window */
	// int height,
	// float **e) /* return values */
	// {
	// int hw = width/2, hh = height/2;
	// int i, j;
	// float diff, diffgradx, diffgrady;
	//
	// /* Set values to zero */
	// for(i = 0; i < 4; i++) e[i][0] = 0.0;
	//
	// /* Compute values */
	// for (j = -hh ; j <= hh ; j++) {
	// for (i = -hw ; i <= hw ; i++) {
	// diff = *imgdiff++;
	// diffgradx = diff * (*gradx++);
	// diffgrady = diff * (*grady++);
	// e[0][0] += diffgradx * i + diffgrady * j;
	// e[1][0] += diffgrady * i - diffgradx * j;
	// e[2][0] += diffgradx;
	// e[3][0] += diffgrady;
	// }
	// }
	//
	// for(i = 0; i < 4; i++) e[i][0] *= 0.5;
	//
	// }
	//
	//
	//
	// /*********************************************************************
	// * _am_trackFeatureAffine
	// *
	// * Tracks a feature point from the image of first occurrence to the actual
	// image.
	// *
	// * RETURNS
	// * KLT_SMALL_DET or KLT_LARGE_RESIDUE or KLT_OOB if feature is lost,
	// * KLT_TRACKED otherwise.
	// */
	//
	// /* if you enalbe the DEBUG_AFFINE_MAPPING make sure you have created a
	// directory "./debug" */
	// /* #define DEBUG_AFFINE_MAPPING */
	//
	// #ifdef DEBUG_AFFINE_MAPPING
	// static int counter = 0;
	// static int glob_index = 0;
	// #endif
	//
	// static int _am_trackFeatureAffine(
	// float x1, /* location of window in first image */
	// float y1,
	// float *x2, /* starting location of search in second image */
	// float *y2,
	// FImage img1,
	// FImage gradx1,
	// FImage grady1,
	// FImage img2,
	// FImage gradx2,
	// FImage grady2,
	// int width, /* size of window */
	// int height,
	// float step_factor, /* 2.0 comes from equations, 1.0 seems to avoid
	// overshooting */
	// int max_iterations,
	// float small, /* determinant threshold for declaring KLT_SMALL_DET */
	// float th, /* displacement threshold for stopping */
	// float th_aff,
	// float max_residue, /* residue threshold for declaring KLT_LARGE_RESIDUE
	// */
	// int lighting_insensitive, /* whether to normalize for gain and bias */
	// int affine_map, /* whether to evaluates the consistency of features with
	// affine mapping */
	// float mdd, /* difference between the displacements */
	// float *Axx, float *Ayx,
	// float *Axy, float *Ayy) /* used affine mapping */
	// {
	//
	//
	// _FloatWindow imgdiff, gradx, grady;
	// float gxx, gxy, gyy, ex, ey, dx, dy;
	// int iteration = 0;
	// int status = 0;
	// int hw = width/2;
	// int hh = height/2;
	// int nc1 = img1.ncols;
	// int nr1 = img1.nrows;
	// int nc2 = img2.ncols;
	// int nr2 = img2.nrows;
	// float **a;
	// float **T;
	// float one_plus_eps = 1.001f; /* To prevent rounding errors */
	// float old_x2 = *x2;
	// float old_y2 = *y2;
	// boolean convergence = false;
	//
	// #ifdef DEBUG_AFFINE_MAPPING
	// char fname[80];
	// FImage aff_diff_win = _KLTCreateFloatImage(width,height);
	// printf("starting location x2=%f y2=%f\n", *x2, *y2);
	// #endif
	//
	// /* Allocate memory for windows */
	// imgdiff = new float[height * width];
	// gradx = new float[height * width];
	// grady = new float[height * width];
	// T = _am_matrix(6,6);
	// a = _am_matrix(6,1);
	//
	// /* Iteratively update the window position */
	// do {
	// if(!affine_map) {
	// /* pure translation tracker */
	//
	// /* If out of bounds, exit loop */
	// if ( x1-hw < 0.0f || nc1-( x1+hw) < one_plus_eps ||
	// *x2-hw < 0.0f || nc2-(*x2+hw) < one_plus_eps ||
	// y1-hh < 0.0f || nr1-( y1+hh) < one_plus_eps ||
	// *y2-hh < 0.0f || nr2-(*y2+hh) < one_plus_eps) {
	// status = KLT_OOB;
	// break;
	// }
	//
	// /* Compute gradient and difference windows */
	// if (lighting_insensitive) {
	// _computeIntensityDifferenceLightingInsensitive(img1, img2, x1, y1, *x2,
	// *y2,
	// width, height, imgdiff);
	// _computeGradientSumLightingInsensitive(gradx1, grady1, gradx2, grady2,
	// img1, img2, x1, y1, *x2, *y2, width, height, gradx, grady);
	// } else {
	// _computeIntensityDifference(img1, img2, x1, y1, *x2, *y2,
	// width, height, imgdiff);
	// _computeGradientSum(gradx1, grady1, gradx2, grady2,
	// x1, y1, *x2, *y2, width, height, gradx, grady);
	// }
	//
	// #ifdef DEBUG_AFFINE_MAPPING
	// aff_diff_win.data = imgdiff;
	// fname = String.format("./debug/kltimg_trans_diff_win%03d.%03d.pgm",
	// glob_index, counter);
	// printf("%s\n", fname);
	// _KLTWriteAbsFloatImageToPGM(aff_diff_win, fname,256.0);
	// printf("iter = %d translation tracker res: %f\n", iteration,
	// _sumAbsFloatWindow(imgdiff, width, height)/(width*height));
	// #endif
	//
	// /* Use these windows to construct matrices */
	// _compute2by2GradientMatrix(gradx, grady, width, height,
	// &gxx, &gxy, &gyy);
	// _compute2by1ErrorVector(imgdiff, gradx, grady, width, height,
	// step_factor,
	// &ex, &ey);
	//
	// /* Using matrices, solve equation for new displacement */
	// status = _solveEquation(gxx, gxy, gyy, ex, ey, small, &dx, &dy);
	//
	// convergence = (fabs(dx) < th && fabs(dy) < th);
	//
	// *x2 += dx;
	// *y2 += dy;
	//
	// }else{
	// /* affine tracker */
	//
	// float ul_x = *Axx * (-hw) + *Axy * hh + *x2; /* upper left corner */
	// float ul_y = *Ayx * (-hw) + *Ayy * hh + *y2;
	// float ll_x = *Axx * (-hw) + *Axy * (-hh) + *x2; /* lower left corner */
	// float ll_y = *Ayx * (-hw) + *Ayy * (-hh) + *y2;
	// float ur_x = *Axx * hw + *Axy * hh + *x2; /* upper right corner */
	// float ur_y = *Ayx * hw + *Ayy * hh + *y2;
	// float lr_x = *Axx * hw + *Axy * (-hh) + *x2; /* lower right corner */
	// float lr_y = *Ayx * hw + *Ayy * (-hh) + *y2;
	//
	// /* If out of bounds, exit loop */
	// if ( x1-hw < 0.0f || nc1-(x1+hw) < one_plus_eps ||
	// y1-hh < 0.0f || nr1-(y1+hh) < one_plus_eps ||
	// ul_x < 0.0f || nc2-(ul_x ) < one_plus_eps ||
	// ll_x < 0.0f || nc2-(ll_x ) < one_plus_eps ||
	// ur_x < 0.0f || nc2-(ur_x ) < one_plus_eps ||
	// lr_x < 0.0f || nc2-(lr_x ) < one_plus_eps ||
	// ul_y < 0.0f || nr2-(ul_y ) < one_plus_eps ||
	// ll_y < 0.0f || nr2-(ll_y ) < one_plus_eps ||
	// ur_y < 0.0f || nr2-(ur_y ) < one_plus_eps ||
	// lr_y < 0.0f || nr2-(lr_y ) < one_plus_eps) {
	// status = KLT_OOB;
	// break;
	// }
	//
	// #ifdef DEBUG_AFFINE_MAPPING
	// counter++;
	// _am_computeAffineMappedImage(img1, x1, y1, 1.0, 0.0 , 0.0, 1.0, width,
	// height, imgdiff);
	// aff_diff_win.data = imgdiff;
	// fname = String.format("./debug/kltimg_aff_diff_win%03d.%03d_1.pgm",
	// glob_index, counter);
	// printf("%s\n", fname);
	// _KLTWriteAbsFloatImageToPGM(aff_diff_win, fname,256.0);
	//
	// _am_computeAffineMappedImage(img2, *x2, *y2, *Axx, *Ayx , *Axy, *Ayy,
	// width, height, imgdiff);
	// aff_diff_win.data = imgdiff;
	// fname = String.format("./debug/kltimg_aff_diff_win%03d.%03d_2.pgm",
	// glob_index, counter);
	// printf("%s\n", fname);
	// _KLTWriteAbsFloatImageToPGM(aff_diff_win, fname,256.0);
	// #endif
	//
	// _am_computeIntensityDifferenceAffine(img1, img2, x1, y1, *x2, *y2, *Axx,
	// *Ayx , *Axy, *Ayy,
	// width, height, imgdiff);
	// #ifdef DEBUG_AFFINE_MAPPING
	// aff_diff_win.data = imgdiff;
	// fname = String.format("./debug/kltimg_aff_diff_win%03d.%03d_3.pgm",
	// glob_index,counter);
	// printf("%s\n", fname);
	// _KLTWriteAbsFloatImageToPGM(aff_diff_win, fname,256.0);
	//
	// printf("iter = %d affine tracker res: %f\n", iteration,
	// _sumAbsFloatWindow(imgdiff, width, height)/(width*height));
	// #endif
	//
	// _am_getGradientWinAffine(gradx2, grady2, *x2, *y2, *Axx, *Ayx , *Axy,
	// *Ayy,
	// width, height, gradx, grady);
	//
	// switch(affine_map){
	// case 1:
	// _am_compute4by1ErrorVector(imgdiff, gradx, grady, width, height, a);
	// _am_compute4by4GradientMatrix(gradx, grady, width, height, T);
	//
	// status = _am_gauss_jordan_elimination(T,4,a,1);
	//
	// *Axx += a[0][0];
	// *Ayx += a[1][0];
	// *Ayy = *Axx;
	// *Axy = -(*Ayx);
	//
	// dx = a[2][0];
	// dy = a[3][0];
	//
	// break;
	// case 2:
	// _am_compute6by1ErrorVector(imgdiff, gradx, grady, width, height, a);
	// _am_compute6by6GradientMatrix(gradx, grady, width, height, T);
	//
	// status = _am_gauss_jordan_elimination(T,6,a,1);
	//
	// *Axx += a[0][0];
	// *Ayx += a[1][0];
	// *Axy += a[2][0];
	// *Ayy += a[3][0];
	//
	// dx = a[4][0];
	// dy = a[5][0];
	//
	// break;
	// }
	//
	// *x2 += dx;
	// *y2 += dy;
	//
	// /* old upper left corner - new upper left corner */
	// ul_x -= *Axx * (-hw) + *Axy * hh + *x2;
	// ul_y -= *Ayx * (-hw) + *Ayy * hh + *y2;
	// /* old lower left corner - new lower left corner */
	// ll_x -= *Axx * (-hw) + *Axy * (-hh) + *x2;
	// ll_y -= *Ayx * (-hw) + *Ayy * (-hh) + *y2;
	// /* old upper right corner - new upper right corner */
	// ur_x -= *Axx * hw + *Axy * hh + *x2;
	// ur_y -= *Ayx * hw + *Ayy * hh + *y2;
	// /* old lower right corner - new lower right corner */
	// lr_x -= *Axx * hw + *Axy * (-hh) + *x2;
	// lr_y -= *Ayx * hw + *Ayy * (-hh) + *y2;
	//
	// #ifdef DEBUG_AFFINE_MAPPING
	// printf
	// ("iter = %d, ul_x=%f ul_y=%f ll_x=%f ll_y=%f ur_x=%f ur_y=%f lr_x=%f lr_y=%f \n",
	// iteration, ul_x, ul_y, ll_x, ll_y, ur_x, ur_y, lr_x, lr_y);
	// #endif
	//
	// convergence = (fabs(dx) < th && fabs(dy) < th &&
	// fabs(ul_x) < th_aff && fabs(ul_y) < th_aff &&
	// fabs(ll_x) < th_aff && fabs(ll_y) < th_aff &&
	// fabs(ur_x) < th_aff && fabs(ur_y) < th_aff &&
	// fabs(lr_x) < th_aff && fabs(lr_y) < th_aff);
	// }
	//
	// if (status == KLT_SMALL_DET) break;
	// iteration++;
	// #ifdef DEBUG_AFFINE_MAPPING
	// printf
	// ("iter = %d, x1=%f, y1=%f, x2=%f, y2=%f,  Axx=%f, Ayx=%f , Axy=%f, Ayy=%f \n",iteration,
	// x1, y1, *x2, *y2, *Axx, *Ayx , *Axy, *Ayy);
	// #endif
	// } while ( !convergence && iteration < max_iterations);
	// /*} while ( (fabs(dx)>=th || fabs(dy)>=th || (affine_map && iteration <
	// 8) ) && iteration < max_iterations); */
	// _am_free_matrix(T);
	// _am_free_matrix(a);
	//
	// /* Check whether window is out of bounds */
	// if (*x2-hw < 0.0f || nc2-(*x2+hw) < one_plus_eps ||
	// *y2-hh < 0.0f || nr2-(*y2+hh) < one_plus_eps)
	// status = KLT_OOB;
	//
	// /* Check whether feature point has moved to much during iteration*/
	// if ( (*x2-old_x2) > mdd || (*y2-old_y2) > mdd )
	// status = KLT_OOB;
	//
	// /* Check whether residue is too large */
	// if (status == KLT_TRACKED) {
	// if(!affine_map){
	// _computeIntensityDifference(img1, img2, x1, y1, *x2, *y2,
	// width, height, imgdiff);
	// }else{
	// _am_computeIntensityDifferenceAffine(img1, img2, x1, y1, *x2, *y2, *Axx,
	// *Ayx , *Axy, *Ayy,
	// width, height, imgdiff);
	// }
	// #ifdef DEBUG_AFFINE_MAPPING
	// printf("iter = %d final_res = %f\n", iteration,
	// _sumAbsFloatWindow(imgdiff, width, height)/(width*height));
	// #endif
	// if (_sumAbsFloatWindow(imgdiff, width, height)/(width*height) >
	// max_residue)
	// status = KLT_LARGE_RESIDUE;
	// }
	//
	// /* Free memory */
	// free(imgdiff); free(gradx); free(grady);
	//
	// #ifdef DEBUG_AFFINE_MAPPING
	// printf("iter = %d status=%d\n", iteration, status);
	// _KLTFreeFloatImage( aff_diff_win );
	// #endif
	//
	// /* Return appropriate value */
	// return status;
	// }
	//
	// /*
	// * CONSISTENCY CHECK OF FEATURES BY AFFINE MAPPING (END)
	// **********************************************************************/
	//
	//
	//
	/*********************************************************************
	 * KLTTrackFeatures
	 * 
	 * Tracks feature points from one image to the next.
	 * 
	 * @param img1
	 * @param img2
	 */
	public void trackFeatures(FImage img1, FImage img2)
	{
		if (isNorm) {
			img1 = img1.multiply(255f);
			img2 = img2.multiply(255f);
		}
		PyramidSet pyr1, pyr2;
		int i;
		final int nrows = img1.height, ncols = img1.width;

		if (KLT_verbose >= 1) {
			System.out.println(String.format("(KLT) Tracking %d features in a %d by %d image...  ",
					featurelist.countRemainingFeatures(), ncols, nrows));
		}

		/* Check window size (and correct if necessary) */
		if (tc.window_width % 2 != 1) {
			tc.window_width = tc.window_width + 1;
			System.out.println(String.format("Tracking context's window width must be odd.  Changing to %d.\n",
					tc.window_width));
		}
		if (tc.window_height % 2 != 1) {
			tc.window_height = tc.window_height + 1;
			System.out.println(String.format("Tracking context's window height must be odd.  Changing to %d.\n",
					tc.window_height));
		}
		if (tc.window_width < 3) {
			tc.window_width = 3;
			System.out.println(String.format(
					"Tracking context's window width must be at least three.  \nChanging to %d.\n", tc.window_width));
		}
		if (tc.window_height < 3) {
			tc.window_height = 3;
			System.out.println(String.format(
					"Tracking context's window height must be at least three.  \nChanging to %d.\n", tc.window_height));
		}

		/* Process first image by converting to float, smoothing, computing */
		/* pyramid, and computing gradient pyramids */
		final PyramidSet ppSet = tc.previousPyramidSet();
		if (tc.sequentialMode && ppSet != null) {
			Pyramid pyramid1, pyramid1_gradx, pyramid1_grady;
			pyramid1 = ppSet.imgPyr;
			pyramid1_gradx = ppSet.gradx;
			pyramid1_grady = ppSet.grady;
			if (pyramid1.ncols[0] != ncols || pyramid1.nrows[0] != nrows)
				throw new RuntimeException(
						String.format("(KLTTrackFeatures) Size of incoming image (%d by %d) is different from size " +
								"of previous image (%d by %d)\n", ncols, nrows, pyramid1.ncols[0], pyramid1.nrows[0]));
			assert (pyramid1_gradx != null);
			assert (pyramid1_grady != null);
			pyr1 = ppSet;
		} else {
			pyr1 = new PyramidSet(img1, tc);

		}

		/* Do the same thing with second image */
		pyr2 = new PyramidSet(img2, tc);

		/* Write internal images */
		if (tc.writeInternalImages) {
			String fname;
			for (i = 0; i < tc.nPyramidLevels; i++) {
				try {
					fname = String.format("kltimg_tf_i%d.png", i);
					ImageUtilities.write(pyr1.imgPyr.img[i], "png", new File(fname));
					fname = String.format("kltimg_tf_i%d_gx.png", i);
					ImageUtilities.write(pyr1.gradx.img[i], "png", new File(fname));
					fname = String.format("kltimg_tf_i%d_gy.png", i);
					ImageUtilities.write(pyr1.grady.img[i], "png", new File(fname));
					fname = String.format("kltimg_tf_j%d.png", i);
					ImageUtilities.write(pyr2.imgPyr.img[i], "png", new File(fname));
					fname = String.format("kltimg_tf_j%d_gx.png", i);
					ImageUtilities.write(pyr2.gradx.img[i], "png", new File(fname));
					fname = String.format("kltimg_tf_j%d_gy.png", i);
					ImageUtilities.write(pyr2.grady.img[i], "png", new File(fname));
				} catch (final IOException e) {

				}
			}
		}

		trackFeatures(img1, img2, pyr1, pyr2);

		if (tc.sequentialMode) {
			tc.setPreviousPyramid(pyr2);
		}

		if (KLT_verbose >= 1) {
			System.err.println(String.format("\n\t%d features successfully tracked.\n",
					featurelist.countRemainingFeatures()));
			if (tc.writeInternalImages)
				System.err.println("\tWrote images to 'kltimg_tf*.pgm'.\n");
		}
	}

	/**
	 * KLTTrackFeatures
	 * 
	 * Tracks feature points from one image to the next. Also takes in image
	 * pyramids and gradient pyramids for both images.
	 * 
	 * @param img1
	 * @param img2
	 * @param pyr1
	 * @param pyr2
	 */
	public void trackFeatures(FImage img1, FImage img2, PyramidSet pyr1, PyramidSet pyr2) {
		float xloc, yloc, xlocout, ylocout;
		int val = -1;
		int indx, r;
		final float subsampling = tc.subsampling;
		final int nrows = img1.height, ncols = img1.width;
		/* For each feature, do ... */
		for (indx = 0; indx < featurelist.features.length; indx++) {

			/* Only track features that are not lost */
			if (featurelist.features[indx].val >= 0) {

				xloc = featurelist.features[indx].x;
				yloc = featurelist.features[indx].y;

				/* Transform location to coarsest resolution */
				for (r = tc.nPyramidLevels - 1; r >= 0; r--) {
					xloc /= subsampling;
					yloc /= subsampling;
				}
				xlocout = xloc;
				ylocout = yloc;

				/* Beginning with coarsest resolution, do ... */
				for (r = tc.nPyramidLevels - 1; r >= 0; r--) {

					/* Track feature at current resolution */
					xloc *= subsampling;
					yloc *= subsampling;
					xlocout *= subsampling;
					ylocout *= subsampling;

					final float[] xylocout = new float[2];
					xylocout[0] = xlocout;
					xylocout[1] = ylocout;

					val = _trackFeature(xloc, yloc,
							xylocout,
							pyr1.imgPyr.img[r],
							pyr1.gradx.img[r], pyr1.grady.img[r],
							pyr2.imgPyr.img[r],
							pyr2.gradx.img[r], pyr2.grady.img[r],
							tc.window_width, tc.window_height,
							tc.step_factor,
							tc.max_iterations,
							tc.min_determinant,
							tc.min_displacement,
							tc.max_residue,
							tc.lighting_insensitive);

					xlocout = xylocout[0];
					ylocout = xylocout[1];

					if (val == KLT_SMALL_DET || val == KLT_OOB)
						break;
				}

				/* Record feature */
				if (val == KLT_OOB) {
					featurelist.features[indx].x = -1.0f;
					featurelist.features[indx].y = -1.0f;
					featurelist.features[indx].val = KLT_OOB;

					// featurelist.features[indx].aff_img = null;
					// featurelist.features[indx].aff_img_gradx = null;
					// featurelist.features[indx].aff_img_grady = null;

				} else if (_outOfBounds(xlocout, ylocout, ncols, nrows, tc.borderx, tc.bordery)) {
					featurelist.features[indx].x = -1.0f;
					featurelist.features[indx].y = -1.0f;
					featurelist.features[indx].val = KLT_OOB;

					// featurelist.features[indx].aff_img = null;
					// featurelist.features[indx].aff_img_gradx = null;
					// featurelist.features[indx].aff_img_grady = null;
				} else if (val == KLT_SMALL_DET) {
					featurelist.features[indx].x = -1.0f;
					featurelist.features[indx].y = -1.0f;
					featurelist.features[indx].val = KLT_SMALL_DET;

					// featurelist.features[indx].aff_img = null;
					// featurelist.features[indx].aff_img_gradx = null;
					// featurelist.features[indx].aff_img_grady = null;
				} else if (val == KLT_LARGE_RESIDUE) {
					featurelist.features[indx].x = -1.0f;
					featurelist.features[indx].y = -1.0f;
					featurelist.features[indx].val = KLT_LARGE_RESIDUE;

					// featurelist.features[indx].aff_img = null;
					// featurelist.features[indx].aff_img_gradx = null;
					// featurelist.features[indx].aff_img_grady = null;
				} else if (val == KLT_MAX_ITERATIONS) {
					featurelist.features[indx].x = -1.0f;
					featurelist.features[indx].y = -1.0f;
					featurelist.features[indx].val = KLT_MAX_ITERATIONS;

					// featurelist.features[indx].aff_img = null;
					// featurelist.features[indx].aff_img_gradx = null;
					// featurelist.features[indx].aff_img_grady = null;
				} else {
					featurelist.features[indx].x = xlocout;
					featurelist.features[indx].y = ylocout;
					featurelist.features[indx].val = KLT_TRACKED;
					if (tc.affineConsistencyCheck >= 0 && val == KLT_TRACKED) { /*
																				 * for
																				 * affine
																				 * mapping
																				 */
						throw new UnsupportedOperationException("Affine mapping not yet implemented");
						// int border = 2; /* add border for interpolation */
						//
						// if(featurelist.features[indx].aff_img == null){
						// /* save image and gradient for each feature at finest
						// resolution after first successful track */
						// featurelist.features[indx].aff_img = new
						// FImage((tc.affine_window_height+border),
						// (tc.affine_window_width+border));
						// featurelist.features[indx].aff_img_gradx = new
						// FImage((tc.affine_window_height+border),
						// (tc.affine_window_width+border));
						// featurelist.features[indx].aff_img_grady = new
						// FImage((tc.affine_window_height+border),
						// (tc.affine_window_width+border));
						// _am_getSubFloatImage(pyramid1.img[0],xloc,yloc,featurelist.features[indx].aff_img);
						// _am_getSubFloatImage(pyramid1_gradx.img[0],xloc,yloc,featurelist.features[indx].aff_img_gradx);
						// _am_getSubFloatImage(pyramid1_grady.img[0],xloc,yloc,featurelist.features[indx].aff_img_grady);
						// featurelist.features[indx].aff_x = xloc - (int) xloc
						// + (tc.affine_window_width+border)/2;
						// featurelist.features[indx].aff_y = yloc - (int) yloc
						// + (tc.affine_window_height+border)/2;;
						// }else{
						// /* affine tracking */
						// val =
						// _am_trackFeatureAffine(featurelist.features[indx].aff_x,
						// featurelist.features[indx].aff_y,
						// &xlocout, &ylocout,
						// featurelist.features[indx].aff_img,
						// featurelist.features[indx].aff_img_gradx,
						// featurelist.features[indx].aff_img_grady,
						// pyramid2.img[0],
						// pyramid2_gradx.img[0], pyramid2_grady.img[0],
						// tc.affine_window_width, tc.affine_window_height,
						// tc.step_factor,
						// tc.affine_max_iterations,
						// tc.min_determinant,
						// tc.min_displacement,
						// tc.affine_min_displacement,
						// tc.affine_max_residue,
						// tc.lighting_insensitive,
						// tc.affineConsistencyCheck,
						// tc.affine_max_displacement_differ,
						// &featurelist.features[indx].aff_Axx,
						// &featurelist.features[indx].aff_Ayx,
						// &featurelist.features[indx].aff_Axy,
						// &featurelist.features[indx].aff_Ayy
						// );
						// featurelist.features[indx].val = val;
						// if(val != KLT_TRACKED){
						// featurelist.features[indx].x = -1.0f;
						// featurelist.features[indx].y = -1.0f;
						// featurelist.features[indx].aff_x = -1.0f;
						// featurelist.features[indx].aff_y = -1.0f;
						//
						// featurelist.features[indx].aff_img = null;
						// featurelist.features[indx].aff_img_gradx = null;
						// featurelist.features[indx].aff_img_grady = null;
						// }else{
						// /*featurelist.features[indx].x = xlocout;*/
						// /*featurelist.features[indx].y = ylocout;*/
						// }
						// }
					}

				}
			}
		}
	}

	/**
	 * @return the tracking context
	 */
	public TrackingContext getTrackingContext() {
		return tc;
	}

	/**
	 * Set the tracking context
	 * 
	 * @param tc
	 */
	public void setTrackingContext(TrackingContext tc) {
		this.tc = tc;
	}

	/**
	 * @return the feature list
	 */
	public FeatureList getFeatureList() {
		return featurelist;
	}

	/**
	 * Set the tracking context
	 * 
	 * @param featurelist
	 */
	public void setFeatureList(FeatureList featurelist) {
		this.featurelist = featurelist;
	}

	/**
	 * @return true if input images are normalised in [0,1]; false if in [0,
	 *         255]
	 */
	public boolean isNorm() {
		return isNorm;
	}

	/**
	 * Set whether input images are in [0,1] (true) or [0,255] (false).
	 * 
	 * @param isNorm
	 */
	public void setNorm(boolean isNorm) {
		this.isNorm = isNorm;
	}

}
