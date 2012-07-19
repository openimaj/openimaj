package org.openimaj.experiment.validation.cross;

import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.experiment.validation.ValidationData;

/**
 * Interface for cross-validation schemes.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <DATASET> The type of dataset
 */
public interface CrossValidationIterable<DATASET extends Dataset<?>> extends Iterable<ValidationData<DATASET>> {

}
