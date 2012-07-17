package org.openimaj.experiment.dataset.crossvalidation;

import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.experiment.dataset.split.TrainSplitProvider;
import org.openimaj.experiment.dataset.split.ValidateSplitProvider;

public class CrossValidationData<T extends Dataset<?>> implements TrainSplitProvider<T>, ValidateSplitProvider<T> {
	T training;
	T validation;

	public CrossValidationData(T training, T validation) {
		this.training = training;
		this.validation = validation;
	}

	@Override
	public T getTrainingDataset() {
		return training;
	}

	@Override
	public T getValidationDataset() {
		return validation;
	}
}
