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
package org.openimaj.math.geometry.transforms;

import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.math.model.EstimatableModel;
import org.openimaj.util.function.Predicate;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;

import Jama.Matrix;

/**
 * Implementation of a Homogeneous Homography model - a transform that models
 * the relationship between planes under projective constraints (8 D.o.F)
 * 
 * @author Jonathon Hare
 * 
 */
public class HomographyModel implements EstimatableModel<Point2d, Point2d>, MatrixTransformProvider {
	protected Predicate<HomographyModel> modelCheck;
	protected Matrix homography = Matrix.identity(3, 3);
	protected boolean normalise;

	/**
	 * Create an {@link HomographyModel} that automatically normalises the data
	 * given to {@link #estimate(List)} to get a numerically stable estimate.
	 */
	public HomographyModel()
	{
		this(true);
	}

	/**
	 * Create a {@link HomographyModel} with optional automatic normalisation.
	 * 
	 * @param norm
	 *            true if the data should be automatically normalised before
	 *            running the DLT algorithm
	 */
	public HomographyModel(boolean norm)
	{
		this.normalise = norm;
	}

	/**
	 * Create an {@link HomographyModel} that automatically normalises the data
	 * given to {@link #estimate(List)} to get a numerically stable estimate.
	 * The given {@link Predicate} is used by the {@link #estimate(List)} method
	 * to test whether the estimated homography is sensible.
	 * 
	 * @param mc
	 *            the test function for sensible homographies
	 */
	public HomographyModel(Predicate<HomographyModel> mc)
	{
		this(true, mc);
	}

	/**
	 * Create a {@link HomographyModel} with optional automatic normalisation.
	 * The given {@link Predicate} is used by the {@link #estimate(List)} method
	 * to test whether the estimated homography is sensible
	 * 
	 * @param norm
	 *            true if the data should be automatically normalised before
	 *            running the DLT algorithm
	 * @param mc
	 *            the test function for sensible homographies
	 */
	public HomographyModel(boolean norm, Predicate<HomographyModel> mc)
	{
		this.normalise = norm;
		this.modelCheck = mc;
	}

	@Override
	public HomographyModel clone() {
		final HomographyModel hm = new HomographyModel(normalise);
		hm.homography = homography.copy();
		return hm;
	}

	@Override
	public Matrix getTransform() {
		return homography;
	}

	/**
	 * Set the transform matrix to the new one
	 * 
	 * @param matrix
	 *            the new matrix
	 */
	public void setTransform(Matrix matrix) {
		homography = matrix;
	}

	/**
	 * DLT estimation of least-squares solution of 3D homogeneous homography
	 * 
	 * @see org.openimaj.math.model.EstimatableModel#estimate(java.util.List)
	 */
	@Override
	public boolean estimate(List<? extends IndependentPair<Point2d, Point2d>> data) {
		if (normalise) {
			homography = TransformUtilities.homographyMatrixNorm(data);
		} else {
			homography = TransformUtilities.homographyMatrix(data);
		}
		if (this.modelCheck == null)
			return true;

		return this.modelCheck.test(this);
	}

	/**
	 * De-normalise a homography estimate. Use when {@link #estimate(List)} was
	 * called with pre-normalised data.
	 * 
	 * @param normalisations
	 *            the normalisation transforms
	 */
	public void denormaliseHomography(Pair<Matrix> normalisations) {
		homography = normalisations.secondObject().inverse().times(homography).times(normalisations.firstObject());
		if (Math.abs(homography.get(2, 2)) > 0.000001) {
			MatrixUtils.times(homography, 1.0 / homography.get(2, 2));
		}
	}

	@Override
	public int numItemsToEstimate() {
		return 4;
	}

	@Override
	public Point2d predict(Point2d data) {
		return data.transform(homography);
	}
}
