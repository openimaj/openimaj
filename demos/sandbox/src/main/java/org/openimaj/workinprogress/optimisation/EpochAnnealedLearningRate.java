package org.openimaj.workinprogress.optimisation;

import org.openimaj.workinprogress.optimisation.params.Parameters;

public class EpochAnnealedLearningRate<PTYPE extends Parameters<PTYPE>> extends LearningRate<PTYPE> {
	private double initialLearningRate;
	private double annealingRate;

	public EpochAnnealedLearningRate(double initialLearningRate, double annealingRate) {
		this.initialLearningRate = initialLearningRate;
		this.annealingRate = annealingRate;
	}

	public EpochAnnealedLearningRate(double initialLearningRate, int maxEpochs) {
		this.initialLearningRate = initialLearningRate;
		this.annealingRate = 0.1 * maxEpochs;
	}

	@Override
	public double getRate(int epoch, int iteration, PTYPE parameters) {
		return initialLearningRate / (1 + epoch / annealingRate);
	}
}