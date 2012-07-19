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
public interface ValidationData<DATASET extends Dataset<?>> extends TrainSplitProvider<DATASET>, ValidateSplitProvider<DATASET> {

}
