package org.openimaj.experiment.dataset.split;

import org.openimaj.experiment.dataset.Dataset;

public interface DatasetSplitter<IN extends Dataset<?>> {
	public void split(IN dataset);
}
