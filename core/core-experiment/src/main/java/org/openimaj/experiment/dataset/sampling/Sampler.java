package org.openimaj.experiment.dataset.sampling;

import org.openimaj.experiment.dataset.Dataset;

/**
 * Interface describing classes that can sample instances
 * from a dataset to create a new (smaller) dataset.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <DATASET> The type of dataset being sampled
 */
public interface Sampler<DATASET extends Dataset<?>> {
	/**
	 * Perform the sampling operation on the given data
	 * and return the sampled dataset.
	 * 
	 * @param dataset the dataset to sample
	 * @return the sampled dataset
	 */
	DATASET sample(DATASET dataset);
}
