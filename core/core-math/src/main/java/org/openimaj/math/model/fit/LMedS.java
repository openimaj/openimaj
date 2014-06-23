package org.openimaj.math.model.fit;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.openimaj.math.model.Model;
import org.openimaj.math.model.fit.error.DefaultModelRelativeError;
import org.openimaj.math.model.fit.error.ModelFitError;
import org.openimaj.math.util.DoubleArrayStatsUtils;
import org.openimaj.util.pair.IndependentPair;

public class LMedS<I, D> implements RobustModelFitting<I, D> {
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

	protected ModelFitError<I, D> errorEstimator = new DefaultModelRelativeError<I, D>();

	protected Model<I, D> model;
	protected Model<I, D> bestModel;
	protected List<IndependentPair<I, D>> inliers = new ArrayList<IndependentPair<I, D>>();
	protected List<IndependentPair<I, D>> outliers = new ArrayList<IndependentPair<I, D>>();

	private double degreesOfFreedom;

	private double bestMedianError;

	public LMedS(Model<I, D> model) {
		this.model = model;
		this.bestModel = model.clone();
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
			errorEstimator.setModel(model);
			model.estimate(sample);

			errorEstimator.computeError(data, errors);

			final double medianError = DoubleArrayStatsUtils.median(errors);
			if (medianError < bestMedianError) {
				bestMedianError = medianError;

				// swap working model and best model
				final Model<I, D> tmp = bestModel;
				bestModel = model;
				model = tmp;

				final double[] tmp2 = bestErrors;
				bestErrors = errors;
				errors = tmp2;
			}
		}

		findInliersOutliers(data, bestErrors);

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
			threshold = 2.0 * sigmahat;
		}

		for (int i = 0; i < data.size(); i++) {
			if (bestErrors[i] < threshold)
				inliers.add(data.get(i));
			else
				outliers.add(data.get(i));
		}
	}

	@Override
	public Model<I, D> getModel() {
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
}
