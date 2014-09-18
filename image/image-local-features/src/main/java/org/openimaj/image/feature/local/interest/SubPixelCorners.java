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
package org.openimaj.image.feature.local.interest;

import java.util.ArrayList;
import java.util.List;

import odk.lang.FastMath;

import org.openimaj.algorithm.iterative.IterationState;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.convolution.FImageConvolveSeparable;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.matrix.PseudoInverse;
import org.openimaj.util.function.Predicate;

import Jama.Matrix;

/**
 * Refines detected corners (i.e. from {@link HarrisIPD} or other methods) to an
 * optimised sub-pixel estimate. The method works by iteratively updating the
 * sub-pixel position of a point based on the inverse of the local gradient
 * auto-correlation matrix.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class SubPixelCorners {
	private static final float GRAD_X_KERNEL[] = { -1.f, 0.f, 1.f };
	private static final float GRAD_Y_KERNEL[] = { 0.f, 0.5f, 0.f };

	private Predicate<IterationState> iter;
	private int halfWidth;
	private int halfHeight;
	private int zeroZoneHalfWidth = -1;
	private int zeroZoneHalfHeight = -1;

	/**
	 * Construct with the given search window size and predicate to <b>stop</b>
	 * the iteration. No zeroing of weights is performed
	 * 
	 * @param halfWidth
	 *            half the search window width (actual size is 2*halfWidth + 1,
	 *            centred on point)
	 * @param halfHeight
	 *            half the search window height (actual size is 2*halfHeight +
	 *            1, centred on point)
	 * @param iter
	 *            the predicate to stop iteration
	 */
	public SubPixelCorners(int halfWidth, int halfHeight, Predicate<IterationState> iter) {
		this.halfWidth = halfWidth;
		this.halfHeight = halfHeight;
		this.iter = iter;
	}

	/**
	 * Construct with the given search window size, zeroed window and predicate
	 * to <b>stop</b> the iteration.
	 * <p>
	 * The zeroed window is a dead region in the middle of the search zone over
	 * which the summation over gradients is not done. It is used sometimes to
	 * avoid possible singularities of the autocorrelation matrix.
	 * 
	 * @param halfWidth
	 *            half the search window width (actual size is 2*halfWidth + 1,
	 *            centred on point)
	 * @param halfHeight
	 *            half the search window height (actual size is 2*halfHeight +
	 *            1, centred on point)
	 * @param zeroZoneHalfWidth
	 *            the half-width of the zeroed region in the weighting array
	 * @param zeroZoneHalfHeight
	 *            the half-height of the zeroed region in the weighting array
	 * @param iter
	 *            the predicate to stop iteration
	 */
	public SubPixelCorners(int halfWidth, int halfHeight, int zeroZoneHalfWidth, int zeroZoneHalfHeight,
			Predicate<IterationState> iter)
	{
		this.halfWidth = halfWidth;
		this.halfHeight = halfHeight;
		this.zeroZoneHalfWidth = zeroZoneHalfWidth;
		this.zeroZoneHalfHeight = zeroZoneHalfHeight;
		this.iter = iter;
	}

	/**
	 * Find the sub-pixel estimated position of each corner
	 * 
	 * @param src
	 *            the image
	 * @param corners
	 *            the initial corner positions
	 * @return the updated corners
	 */
	public List<Point2dImpl> findSubPixCorners(FImage src, List<? extends Point2d> corners)
	{
		final List<Point2dImpl> outCorners = new ArrayList<Point2dImpl>(corners.size());
		final int windowWidth = halfWidth * 2 + 1;
		final int windowHeight = halfHeight * 2 + 1;

		if (corners.size() == 0)
			return outCorners;

		final FImage weights = this.buildGaussianWeights(windowWidth, windowHeight);
		final FImage gx = new FImage(windowWidth, windowHeight);
		final FImage gy = new FImage(windowWidth, windowHeight);
		final float[] buffer = new float[windowWidth + 2];

		// note 2px padding as conv reduces size:
		final FImage roi = new FImage(windowWidth + 2, windowHeight + 2);

		// loop for all the points
		for (int i = 0; i < corners.size(); i++)
		{
			final Point2d pt = corners.get(i);
			outCorners.add(this.findCornerSubPix(src, pt, roi, gx, gy, weights, buffer));
		}

		return outCorners;
	}

	/**
	 * Find the sub-pixel estimated position of a corner
	 * 
	 * @param src
	 *            the image
	 * @param corner
	 *            the initial corner position
	 * @return the updated corner position
	 */
	public Point2dImpl findSubPixCorner(FImage src, Point2d corner)
	{
		final int windowWidth = halfWidth * 2 + 1;
		final int windowHeight = halfHeight * 2 + 1;

		final FImage weights = this.buildGaussianWeights(windowWidth, windowHeight);
		final FImage gx = new FImage(windowWidth, windowHeight);
		final FImage gy = new FImage(windowWidth, windowHeight);
		final float[] buffer = new float[windowWidth + 2];

		// note 2px padding as conv reduces size:
		final FImage roi = new FImage(windowWidth + 2, windowHeight + 2);

		return this.findCornerSubPix(src, corner, roi, gx, gy, weights, buffer);
	}

	/**
	 * Build the Gaussian weighting image
	 * 
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @return
	 */
	private FImage buildGaussianWeights(int width, int height) {
		final FImage weights = new FImage(width, height);
		final float[] weightsX = new float[width];

		double coeff = 1. / (halfWidth * halfWidth);
		for (int i = -halfWidth, k = 0; i <= halfWidth; i++, k++)
		{
			weightsX[k] = (float) Math.exp(-i * i * coeff);
		}

		float[] weightsY;
		if (halfWidth == halfHeight) {
			weightsY = weightsX;
		} else {
			weightsY = new float[height];
			coeff = 1. / (halfHeight * halfHeight);
			for (int i = -halfHeight, k = 0; i <= halfHeight; i++, k++)
			{
				weightsY[k] = (float) Math.exp(-i * i * coeff);
			}
		}

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				weights.pixels[i][j] = weightsX[j] * weightsY[i];
			}
		}

		// if necessary, mask off the centre portion
		if (zeroZoneHalfWidth >= 0 && zeroZoneHalfHeight >= 0 &&
				zeroZoneHalfWidth * 2 + 1 < width && zeroZoneHalfHeight * 2 + 1 < height)
		{
			for (int i = halfHeight - zeroZoneHalfHeight; i <= halfHeight + zeroZoneHalfHeight; i++) {
				for (int j = halfWidth - zeroZoneHalfWidth; j <= halfWidth + zeroZoneHalfWidth; j++) {
					weights.pixels[i][j] = 0;
				}
			}
		}

		return weights;
	}

	private Point2dImpl findCornerSubPix(FImage src, Point2d pt, FImage roi, FImage gx, FImage gy, FImage weights,
			final float[] buffer)
	{
		final IterationState state = new IterationState();
		Point2dImpl current = new Point2dImpl(pt);

		while (!iter.test(state)) {
			// get window around pt
			src.extractCentreSubPix(current, roi);

			// calc derivatives
			FImageConvolveSeparable.fastConvolve3(roi, gx, GRAD_X_KERNEL, GRAD_Y_KERNEL, buffer);
			FImageConvolveSeparable.fastConvolve3(roi, gy, GRAD_Y_KERNEL, GRAD_X_KERNEL, buffer);

			double a = 0, b = 0, c = 0, bb1 = 0, bb2 = 0;
			final int win_w = weights.width;
			final int win_h = weights.height;

			// process gradient
			for (int i = 0; i < win_h; i++)
			{
				final double py = i - halfHeight;

				for (int j = 0; j < win_w; j++)
				{
					final double m = weights.pixels[i][j];
					final double tgx = gx.pixels[i][j];
					final double tgy = gy.pixels[i][j];
					final double gxx = tgx * tgx * m;
					final double gxy = tgx * tgy * m;
					final double gyy = tgy * tgy * m;
					final double px = j - halfWidth;

					a += gxx;
					b += gxy;
					c += gyy;

					bb1 += gxx * px + gxy * py;
					bb2 += gxy * px + gyy * py;
				}
			}

			final Matrix m = new Matrix(new double[][] { { a, b }, { b, c } });
			final Matrix mInv = PseudoInverse.pseudoInverse(m);
			final Point2dImpl cI2 = new Point2dImpl();
			cI2.x = (float) (current.x + mInv.get(0, 0) * bb1 + mInv.get(0, 1) * bb2);
			cI2.y = (float) (current.y + mInv.get(1, 0) * bb1 + mInv.get(1, 1) * bb2);

			state.epsilon = FastMath.sqrt((cI2.x - current.x) * (cI2.x - current.x) + (cI2.y - current.y)
					* (cI2.y - current.y));
			state.iteration++;
			current = cI2;
		}

		// if new point is too far from initial, it means poor convergence.
		// return initial point as the result
		if (Math.abs(current.x - pt.getX()) > halfWidth || Math.abs(current.y - pt.getY()) > halfHeight)
		{
			return new Point2dImpl(pt);
		}

		// otherwise return the updated point
		return current;
	}
}
