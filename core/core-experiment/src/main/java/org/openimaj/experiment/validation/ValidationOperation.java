package org.openimaj.experiment.validation;

import org.openimaj.experiment.dataset.Dataset;

/**
 * Interface describing the contract for classes which can
 * perform a the operations required for a single round
 * of validation.
 * <p>
 * The common use-case of this class is for it to be used
 * as part of the validation of some system as coordinated
 * by a {@link ValidationRunner}. As the {@link ValidationRunner}
 * can perform multiple validations at once using multi-threading,
 * implementors of this interface should be thread-safe. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <DATASET> The type of dataset
 * @param <ANALYSIS_RESULT> The type of the resulting data from the operation.
 */
public interface ValidationOperation<DATASET extends Dataset<?>, ANALYSIS_RESULT> {
	/**
	 * Perform the operations required for one round of validation
	 * using the provided training and validation datasets. The
	 * result of the evaluation round is returned.
	 * 
	 * @param training the training data
	 * @param validation the validation data
	 * @return the result of the validation round
	 */
	public abstract ANALYSIS_RESULT evaluate(DATASET training, DATASET validation);		
}
