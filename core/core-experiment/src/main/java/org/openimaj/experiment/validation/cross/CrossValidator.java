package org.openimaj.experiment.validation.cross;

import java.util.Iterator;

import org.openimaj.experiment.dataset.Dataset;

/**
 * Interface for cross-validation schemes. A scheme must
 * be able to create an {@link Iterable} that provides
 * an {@link Iterator} over the different sets of training
 * and validation data created during the cross validation
 * process.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <DATASET> The type of dataset
 */
public interface CrossValidator<DATASET extends Dataset<?>> {
	/**
	 * Create the iterable from the dataset.
	 * @param data the dataset
	 * @return the iterable 
	 */
	public CrossValidationIterable<DATASET> createIterable(DATASET data);
}
