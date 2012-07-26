package org.openimaj.experiment.validation.cross;

import java.util.Iterator;

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
	/**
	 * Get the number of iterations that the {@link Iterator}
	 * returned by {@link #iterator()} will perform.
	 * 
	 * @return the number of iterations that will be performed
	 */
	public int numberIterations();
}
