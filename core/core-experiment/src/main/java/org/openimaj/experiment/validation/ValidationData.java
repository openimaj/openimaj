package org.openimaj.experiment.validation;

import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.experiment.dataset.split.TrainSplitProvider;
import org.openimaj.experiment.dataset.split.ValidateSplitProvider;

/**
 * Data for performing validation. Contains datasets for 
 * training and validation.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <DATASET> Type of the {@link Dataset}s
 */
public class ValidationData<DATASET extends Dataset<?>> implements TrainSplitProvider<DATASET>, ValidateSplitProvider<DATASET> {
	private DATASET training;
	private DATASET validation;

	/**
	 * Construct with the training and validation datasets.
	 * 
	 * @param training the training dataset
	 * @param validation the validation dataset
	 */
	public ValidationData(DATASET training, DATASET validation) {
		this.training = training;
		this.validation = validation;
	}

	@Override
	public DATASET getTrainingDataset() {
		return training;
	}

	@Override
	public DATASET getValidationDataset() {
		return validation;
	}
}
