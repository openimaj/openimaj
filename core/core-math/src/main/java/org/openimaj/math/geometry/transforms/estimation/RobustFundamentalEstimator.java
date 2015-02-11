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

import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.transforms.FundamentalModel;
import org.openimaj.math.geometry.transforms.FundamentalRefinement;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.geometry.transforms.estimation.sampling.BucketingSampler2d;
import org.openimaj.math.geometry.transforms.residuals.AlgebraicResidual2d;
import org.openimaj.math.model.fit.LMedS;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.math.model.fit.RANSAC.StoppingCondition;
import org.openimaj.math.model.fit.RobustModelFitting;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;

import Jama.Matrix;

/**
 * Helper class to simplify robust estimation of the Fundamental matrix without
 * having to deal with the nuts and bolts of the underlying robust model
 * fitters, etc. The overall robust estimation process is as follows:
 * <p>
 * An initial estimate of the inliers and an algebraically optimal Fundamental
 * matrix is computed using {@link RANSAC} or {@link LMedS} with a
 * {@link BucketingSampler2d} sampling strategy for selecting subsets. In both
 * cases, the normalised 8-point algorithm is used (see
 * {@link TransformUtilities#fundamentalMatrix8PtNorm(List)}
 * <p>
 * If an reasonable initial estimate was found, non-linear optimisation using
 * Levenburg-Marquardt is performed to on the inliers using the initial estimate
 * to optimise against a true geometric residual given by
 * {@link FundamentalRefinement}.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class RobustFundamentalEstimator implements RobustModelFitting<Point2d, Point2d, FundamentalModel> {
	private RobustModelFitting<Point2d, Point2d, FundamentalModel> robustFitter;
	private FundamentalRefinement refinement;

	List<IndependentPair<Point2d, Point2d>> inliers = new ArrayList<IndependentPair<Point2d, Point2d>>();
	List<IndependentPair<Point2d, Point2d>> outliers = new ArrayList<IndependentPair<Point2d, Point2d>>();

	/**
	 * Construct using the {@link LMedS} algorithm with the given expected
	 * outlier percentage
	 *
	 * @param outlierProportion
	 *            expected proportion of outliers (between 0 and 1)
	 * @param refinement
	 *            the refinement technique
	 */
	public RobustFundamentalEstimator(double outlierProportion, FundamentalRefinement refinement) {
		robustFitter = new LMedS<Point2d, Point2d, FundamentalModel>(
				new FundamentalModel(false),
				new FundamentalModel.Fundamental8PtResidual(),
				outlierProportion, true, new BucketingSampler2d());

		this.refinement = refinement;
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
	 * @param refinement
	 *            the refinement technique
	 */
	public RobustFundamentalEstimator(double threshold, int nIterations, StoppingCondition stoppingCondition,
			FundamentalRefinement refinement)
	{
		robustFitter = new RANSAC<Point2d, Point2d, FundamentalModel>(new FundamentalModel(false),
				new FundamentalModel.Fundamental8PtResidual(), threshold, nIterations, stoppingCondition, true,
				new BucketingSampler2d());

		this.refinement = refinement;
	}

	@Override
	public boolean fitData(List<? extends IndependentPair<Point2d, Point2d>> data) {
		final Pair<Matrix> norms = TransformUtilities.getNormalisations(data);
		final List<? extends IndependentPair<Point2d, Point2d>> normData = TransformUtilities.normalise(data, norms);

		// Use a robust fitting technique to find the inliers and estimate a
		// model using DLT
		if (!robustFitter.fitData(normData)) {
			// just go with full-on DLT estimate rather than a robust one
			robustFitter.getModel().estimate(normData);
			robustFitter.getModel().denormaliseFundamental(norms);

			return false;
		}

		// remap the inliers and outliers from the normalised ones to the
		// original space
		inliers.clear();
		for (final IndependentPair<Point2d, Point2d> pair : robustFitter.getInliers()) {
			inliers.add(data.get(normData.indexOf(pair)));
		}
		outliers.clear();
		for (final IndependentPair<Point2d, Point2d> pair : robustFitter.getOutliers()) {
			outliers.add(data.get(normData.indexOf(pair)));
		}

		// denormalise the estimated matrix before the non-linear step
		robustFitter.getModel().denormaliseFundamental(norms);

		// Now apply non-linear optimisation to get a better estimate
		final Matrix optimised = refinement.refine(robustFitter.getModel().getF(), inliers);
		robustFitter.getModel().setF(optimised);

		return true;
	}

	@Override
	public int numItemsToEstimate() {
		return robustFitter.numItemsToEstimate();
	}

	@Override
	public FundamentalModel getModel() {
		return robustFitter.getModel();
	}

	@Override
	public List<? extends IndependentPair<Point2d, Point2d>> getInliers() {
		return inliers;
	}

	@Override
	public List<? extends IndependentPair<Point2d, Point2d>> getOutliers() {
		return outliers;
	}
}
