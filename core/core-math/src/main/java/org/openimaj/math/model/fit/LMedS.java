package org.openimaj.math.model.fit;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.math.model.EstimatableModel;
import org.openimaj.math.model.fit.residuals.ResidualCalculator;
import org.openimaj.math.util.DoubleArrayStatsUtils;
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

	private double bestMedianError;

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
	 */
	@SuppressWarnings("unchecked")
	public LMedS(M model, ResidualCalculator<I, D, M> residualEstimator, boolean impEst) {
		this.model = model;
		this.residualEstimator = residualEstimator;
		this.bestModel = (M) model.clone();
		this.improveEstimate = impEst;
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
	 */
	public LMedS(M model, ResidualCalculator<I, D, M> residualEstimator, double outlierProportion, boolean impEst) {
		this(model, residualEstimator, impEst);
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
	 */
	public LMedS(M model, ResidualCalculator<I, D, M> residualEstimator, double outlierProportion,
			double inlierNoiseLevel, double degreesOfFreedom, boolean impEst)
	{
		this(model, residualEstimator, outlierProportion, impEst);
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
		bestMedianError = Double.MAX_VALUE;

		for (int i = 0; i < numSamples; i++) {
			final List<? extends IndependentPair<I, D>> sample = RANSAC.getRandomItems(data, sampleSize);
			model.estimate(sample);

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
			bestModel.estimate(inliers);
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
			final double sigmahat = 1.4826 * (1 + 5 / (data.size() - model.numItemsToEstimate()))
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
