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
package org.openimaj.math.model.fit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.math.model.EstimatableModel;
import org.openimaj.math.model.fit.residuals.ResidualCalculator;
import org.openimaj.math.util.DoubleArrayStatsUtils;
import org.openimaj.util.CollectionSampler;
import org.openimaj.util.UniformSampler;
import org.openimaj.util.pair.IndependentPair;

/**
 * Least Median of Squares robust model fitting
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <I>
 *            type of independent data
 * @param <D>
 *            type of dependent data
 * @param <M>
 *            concrete type of model learned
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Peter J. Rousseeuw" },
		title = "Least Median of Squares Regression",
		year = "1984",
		journal = "Journal of the American Statistical Association",
		pages = { "871", "", "880" },
		url = "http://www.jstor.org/stable/2288718",
		month = "December",
		number = "388",
		volume = "79")
public class LMedS<I, D, M extends EstimatableModel<I, D>> implements RobustModelFitting<I, D, M> {
	/**
	 * Probability that at least one of the samples drawn is good
	 */
	double probability = 0.99;

	/**
	 * The level of inlier noise (standard deviation of Gaussian noise inherent
	 * in the data). Set to -1 if unknown.
	 */
	double inlierNoiseLevel = -1;

	/**
	 * Expected proportion of outliers
	 */
	double outlierProportion = 0.4;

	/**
	 * Number of degrees of freedom of the model; used in conjunction with
	 * inlierNoiseLevel if set to estimate the inliers.
	 */
	private double degreesOfFreedom;

	protected ResidualCalculator<I, D, M> residualEstimator;

	protected boolean improveEstimate;

	protected M model;
	protected M bestModel;
	protected List<IndependentPair<I, D>> inliers = new ArrayList<IndependentPair<I, D>>();
	protected List<IndependentPair<I, D>> outliers = new ArrayList<IndependentPair<I, D>>();
	protected CollectionSampler<IndependentPair<I, D>> sampler;
	private double bestMedianError;

	/**
	 * Construct with the given model and residual calculator. The proportion of
	 * outliers is assumed to be 0.4. The threshold for determining inliers and
	 * outliers is automatically computed. Uniform random sampling is used for
	 * creating the subsets.
	 *
	 * @param model
	 *            the model to estimate
	 * @param residualEstimator
	 *            the algorithm to compute residuals of the model
	 * @param impEst
	 *            True if we want to perform a final fitting of the model with
	 *            all inliers, false otherwise
	 */
	public LMedS(M model, ResidualCalculator<I, D, M> residualEstimator, boolean impEst) {
		this(model, residualEstimator, impEst, new UniformSampler<IndependentPair<I, D>>());
	}

	/**
	 * Construct with the given model, residual calculator and estimated
	 * proportion of outliers. The threshold for determining inliers and
	 * outliers is automatically computed. Uniform random sampling is used for
	 * creating the subsets.
	 *
	 * @param model
	 *            the model to estimate
	 * @param residualEstimator
	 *            the algorithm to compute residuals of the model
	 * @param outlierProportion
	 *            Expected proportion of outliers
	 * @param impEst
	 *            True if we want to perform a final fitting of the model with
	 *            all inliers, false otherwise
	 */
	public LMedS(M model, ResidualCalculator<I, D, M> residualEstimator, double outlierProportion, boolean impEst) {
		this(model, residualEstimator, impEst);
		this.outlierProportion = outlierProportion;
	}

	/**
	 * Construct with the given model, residual calculator and estimated
	 * proportion of outliers. The given inlier noise level and number of
	 * degrees of freedom of the model are used to estimate the optimal
	 * threshold for determining inliers versus outliers. Uniform random
	 * sampling is used for creating the subsets.
	 *
	 * @param model
	 *            the model to estimate
	 * @param residualEstimator
	 *            the algorithm to compute residuals of the model
	 * @param outlierProportion
	 *            Expected proportion of outliers
	 * @param inlierNoiseLevel
	 *            The level of inlier noise (standard deviation of Gaussian
	 *            noise inherent in the data).
	 * @param degreesOfFreedom
	 *            Number of degrees of freedom of the model
	 * @param impEst
	 *            True if we want to perform a final fitting of the model with
	 *            all inliers, false otherwise
	 */
	public LMedS(M model, ResidualCalculator<I, D, M> residualEstimator, double outlierProportion,
			double inlierNoiseLevel, double degreesOfFreedom, boolean impEst)
	{
		this(model, residualEstimator, outlierProportion, impEst);
		this.inlierNoiseLevel = inlierNoiseLevel;
		this.degreesOfFreedom = degreesOfFreedom;
	}

	/**
	 * Construct with the given model and residual calculator. The proportion of
	 * outliers is assumed to be 0.4. The threshold for determining inliers and
	 * outliers is automatically computed.
	 *
	 * @param model
	 *            the model to estimate
	 * @param residualEstimator
	 *            the algorithm to compute residuals of the model
	 * @param impEst
	 *            True if we want to perform a final fitting of the model with
	 *            all inliers, false otherwise
	 * @param sampler
	 *            the sampling algorithm for selecting random subsets
	 */
	@SuppressWarnings("unchecked")
	public LMedS(M model, ResidualCalculator<I, D, M> residualEstimator, boolean impEst,
			CollectionSampler<IndependentPair<I, D>> sampler)
	{
		this.model = model;
		this.residualEstimator = residualEstimator;
		this.bestModel = (M) model.clone();
		this.improveEstimate = impEst;
		this.sampler = sampler;
	}

	/**
	 * Construct with the given model, residual calculator and estimated
	 * proportion of outliers. The threshold for determining inliers and
	 * outliers is automatically computed.
	 *
	 * @param model
	 *            the model to estimate
	 * @param residualEstimator
	 *            the algorithm to compute residuals of the model
	 * @param outlierProportion
	 *            Expected proportion of outliers
	 * @param impEst
	 *            True if we want to perform a final fitting of the model with
	 *            all inliers, false otherwise
	 * @param sampler
	 *            the sampling algorithm for selecting random subsets
	 */
	public LMedS(M model, ResidualCalculator<I, D, M> residualEstimator, double outlierProportion, boolean impEst,
			CollectionSampler<IndependentPair<I, D>> sampler)
	{
		this(model, residualEstimator, impEst, sampler);
		this.outlierProportion = outlierProportion;
	}

	/**
	 * Construct with the given model, residual calculator and estimated
	 * proportion of outliers. The given inlier noise level and number of
	 * degrees of freedom of the model are used to estimate the optimal
	 * threshold for determining inliers versus outliers.
	 *
	 * @param model
	 *            the model to estimate
	 * @param residualEstimator
	 *            the algorithm to compute residuals of the model
	 * @param outlierProportion
	 *            Expected proportion of outliers
	 * @param inlierNoiseLevel
	 *            The level of inlier noise (standard deviation of Gaussian
	 *            noise inherent in the data).
	 * @param degreesOfFreedom
	 *            Number of degrees of freedom of the model
	 * @param impEst
	 *            True if we want to perform a final fitting of the model with
	 *            all inliers, false otherwise
	 * @param sampler
	 *            the sampling algorithm for selecting random subsets
	 */
	public LMedS(M model, ResidualCalculator<I, D, M> residualEstimator, double outlierProportion,
			double inlierNoiseLevel, double degreesOfFreedom, boolean impEst,
			CollectionSampler<IndependentPair<I, D>> sampler)
	{
		this(model, residualEstimator, outlierProportion, impEst, sampler);
		this.inlierNoiseLevel = inlierNoiseLevel;
		this.degreesOfFreedom = degreesOfFreedom;
	}

	@Override
	public boolean fitData(List<? extends IndependentPair<I, D>> data) {
		final int sampleSize = model.numItemsToEstimate();

		if (data.size() < sampleSize)
			return false;

		final int numSamples = (int) Math.ceil(Math.log(1 - probability)
				/ Math.log(1 - Math.pow(1 - outlierProportion, sampleSize)));

		double[] errors = new double[data.size()];
		double[] bestErrors = new double[data.size()];
		Arrays.fill(bestErrors, Double.MAX_VALUE);
		bestMedianError = Double.MAX_VALUE;

		sampler.setCollection(data);

		for (int i = 0; i < numSamples; i++) {
			final List<? extends IndependentPair<I, D>> sample = sampler.sample(sampleSize);

			if (!model.estimate(sample))
				continue;

			residualEstimator.setModel(model);
			residualEstimator.computeResiduals(data, errors);

			final double medianError = DoubleArrayStatsUtils.median(errors);
			if (medianError < bestMedianError) {
				bestMedianError = medianError;

				// swap working model and best model
				final M tmp = bestModel;
				bestModel = model;
				model = tmp;

				final double[] tmp2 = bestErrors;
				bestErrors = errors;
				errors = tmp2;
			}
		}

		findInliersOutliers(data, bestErrors);

		if (improveEstimate) {
			if (!bestModel.estimate(inliers))
				return false;
		}

		final double outlierProp = (double) outliers.size() / (double) data.size();

		return outlierProp < this.outlierProportion;
	}

	private void findInliersOutliers(List<? extends IndependentPair<I, D>> data, double[] bestErrors) {
		inliers.clear();
		outliers.clear();

		double threshold;
		if (inlierNoiseLevel > 0)
			threshold = inlierNoiseLevel * inlierNoiseLevel
					* new ChiSquaredDistribution(degreesOfFreedom).inverseCumulativeProbability(probability);
		else {
			final double sigmahat = 1.4826 * (1 + 5 / (Math.max(1, data.size() - model.numItemsToEstimate())))
					* Math.sqrt(bestMedianError);
			// http://research.microsoft.com/en-us/um/people/zhang/INRIA/Publis/Tutorial-Estim/node25.html
			threshold = (2.5 * sigmahat) * (2.5 * sigmahat);
		}

		for (int i = 0; i < data.size(); i++) {
			if (bestErrors[i] < threshold)
				inliers.add(data.get(i));
			else
				outliers.add(data.get(i));
		}
	}

	@Override
	public M getModel() {
		return bestModel;
	}

	@Override
	public List<? extends IndependentPair<I, D>> getInliers() {
		return inliers;
	}

	@Override
	public List<? extends IndependentPair<I, D>> getOutliers() {
		return outliers;
	}

	@Override
	public int numItemsToEstimate() {
		return model.numItemsToEstimate();
	}
}
