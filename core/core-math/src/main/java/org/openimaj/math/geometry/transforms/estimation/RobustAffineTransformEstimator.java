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
package org.openimaj.math.geometry.transforms.estimation;

import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.transforms.AffineTransformModel;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.geometry.transforms.estimation.sampling.BucketingSampler2d;
import org.openimaj.math.geometry.transforms.residuals.AlgebraicResidual2d;
import org.openimaj.math.model.fit.LMedS;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.math.model.fit.RANSAC.StoppingCondition;
import org.openimaj.math.model.fit.RobustModelFitting;
import org.openimaj.util.function.Predicate;
import org.openimaj.util.pair.IndependentPair;

/**
 * Helper class to simplify robust estimation of 2D affine transforms without
 * having to deal with the nuts and bolts of the underlying robust model
 * fitters, etc. The overall robust estimation process uses the normalised DLT
 * algorithm (see {@link TransformUtilities#affineMatrix(List)}) coupled with
 * either {@link RANSAC} or {@link LMedS} with a {@link BucketingSampler2d}
 * sampling strategy for selecting subsets.
 * <p>
 * Non-linear optimisation is unncessary as the algebraic and geometric
 * distances are equal in the affine case.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class RobustAffineTransformEstimator implements RobustModelFitting<Point2d, Point2d, AffineTransformModel> {
	private RobustModelFitting<Point2d, Point2d, AffineTransformModel> robustFitter;

	/**
	 * Construct using the {@link LMedS} algorithm with the given expected
	 * outlier percentage
	 * 
	 * @param outlierProportion
	 *            expected proportion of outliers (between 0 and 1)
	 */
	public RobustAffineTransformEstimator(double outlierProportion) {
		robustFitter = new LMedS<Point2d, Point2d, AffineTransformModel>(
				new AffineTransformModel(),
				new AlgebraicResidual2d<AffineTransformModel>(),
				outlierProportion, true, new BucketingSampler2d());
	}

	/**
	 * Construct using the {@link RANSAC} algorithm with the given options.
	 * 
	 * @param threshold
	 *            the threshold on the {@link AlgebraicResidual2d} at which to
	 *            consider a point as an inlier
	 * @param nIterations
	 *            the maximum number of iterations
	 * @param stoppingCondition
	 *            the {@link StoppingCondition} for RANSAC
	 */
	public RobustAffineTransformEstimator(double threshold, int nIterations, StoppingCondition stoppingCondition)
	{
		robustFitter = new RANSAC<Point2d, Point2d, AffineTransformModel>(new AffineTransformModel(),
				new AlgebraicResidual2d<AffineTransformModel>(), threshold, nIterations, stoppingCondition, true,
				new BucketingSampler2d());
	}

	/**
	 * Construct using the {@link LMedS} algorithm with the given expected
	 * outlier percentage
	 * 
	 * @param outlierProportion
	 *            expected proportion of outliers (between 0 and 1)
	 * @param modelCheck
	 *            the predicate to test whether an estimated model is sane
	 */
	public RobustAffineTransformEstimator(double outlierProportion, Predicate<AffineTransformModel> modelCheck) {
		robustFitter = new LMedS<Point2d, Point2d, AffineTransformModel>(
				new AffineTransformModel(modelCheck),
				new AlgebraicResidual2d<AffineTransformModel>(),
				outlierProportion, true, new BucketingSampler2d());
	}

	/**
	 * Construct using the {@link RANSAC} algorithm with the given options.
	 * 
	 * @param threshold
	 *            the threshold on the {@link AlgebraicResidual2d} at which to
	 *            consider a point as an inlier
	 * @param nIterations
	 *            the maximum number of iterations
	 * @param stoppingCondition
	 *            the {@link StoppingCondition} for RANSAC
	 * @param modelCheck
	 *            the predicate to test whether an estimated model is sane
	 */
	public RobustAffineTransformEstimator(double threshold, int nIterations, StoppingCondition stoppingCondition,
			Predicate<AffineTransformModel> modelCheck)
	{
		robustFitter = new RANSAC<Point2d, Point2d, AffineTransformModel>(new AffineTransformModel(modelCheck),
				new AlgebraicResidual2d<AffineTransformModel>(), threshold, nIterations, stoppingCondition, true,
				new BucketingSampler2d());
	}

	@Override
	public boolean fitData(List<? extends IndependentPair<Point2d, Point2d>> data) {
		// Use a robust fitting technique to find the inliers and estimate a
		// model using DLT
		if (!robustFitter.fitData(data)) {
			// just go with full-on DLT estimate rather than a robust one
			robustFitter.getModel().estimate(data);

			return false;
		}

		return true;
	}

	@Override
	public int numItemsToEstimate() {
		return robustFitter.numItemsToEstimate();
	}

	@Override
	public AffineTransformModel getModel() {
		return robustFitter.getModel();
	}

	@Override
	public List<? extends IndependentPair<Point2d, Point2d>> getInliers() {
		return robustFitter.getInliers();
	}

	@Override
	public List<? extends IndependentPair<Point2d, Point2d>> getOutliers() {
		return robustFitter.getOutliers();
	}
}
