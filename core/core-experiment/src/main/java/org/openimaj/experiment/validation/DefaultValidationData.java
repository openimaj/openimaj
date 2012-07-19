package org.openimaj.experiment.validation;

import org.openimaj.experiment.dataset.Dataset;

/**
 * Data for performing validation. Contains datasets for 
 * training and validation.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <DATASET> Type of the {@link Dataset}s
 */
public class DefaultValidationData<DATASET extends Dataset<?>> implements ValidationData<DATASET> {
	protected DATASET training;
	protected DATASET validation;

	protected DefaultValidationData() {
	}
	
	/**
	 * Construct with the training and validation datasets.
	 * 
	 * @param training the training dataset
	 * @param validation the validation dataset
	 */
	public DefaultValidationData(DATASET training, DATASET validation) {
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
