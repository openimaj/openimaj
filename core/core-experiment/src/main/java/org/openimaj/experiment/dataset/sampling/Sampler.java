package org.openimaj.experiment.dataset.sampling;

import org.openimaj.experiment.dataset.Dataset;

public interface Sampler<DATASET extends Dataset<?>> {
	DATASET sample(DATASET dataset);
}
