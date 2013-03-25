package org.openimaj.demos.sandbox.dataset;

import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.io.ObjectReader;

/**
 * Base class for {@link ListDataset}s in which each instance is read with an
 * {@link ObjectReader}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <INSTANCE>
 *            the type of instances in the dataset
 */
abstract class ReadableListDataset<INSTANCE> implements ListDataset<INSTANCE> {
	protected ObjectReader<INSTANCE> reader;

	public ReadableListDataset(ObjectReader<INSTANCE> reader) {
		this.reader = reader;
	}

	@Override
	public INSTANCE getRandomInstance() {
		return getInstance((int) (Math.random() * size()));
	}
}
