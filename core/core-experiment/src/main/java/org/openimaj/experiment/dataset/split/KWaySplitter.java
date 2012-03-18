package org.openimaj.experiment.dataset.split;

import org.openimaj.experiment.dataset.Dataset;

public interface KWaySplitter<T extends Dataset<?>> extends DatasetSplitter<T> {
	public T getSplit(int i);
}
