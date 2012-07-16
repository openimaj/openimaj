package org.openimaj.experiment.dataset.crossvalidation;

import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.experiment.dataset.Identifiable;
import org.openimaj.experiment.dataset.ListDataset;

public class CrossValidationData<T extends Dataset<?>> {
	T training;
	T validation;

	public CrossValidationData(T training, T validation) {
		this.training = training;
		this.validation = validation;
	}

	/**
	 * @return the training data
	 */
	public T getTrainingDataset() {
		return training;
	}

	/**
	 * @return the validation data
	 */
	public T getValidationDataset() {
		return validation;
	}
}
