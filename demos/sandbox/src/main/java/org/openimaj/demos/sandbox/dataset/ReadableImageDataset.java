package org.openimaj.demos.sandbox.dataset;

import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.image.Image;
import org.openimaj.io.ObjectReader;

public abstract class ReadableImageDataset<IMAGE extends Image<?, IMAGE>> implements ListDataset<IMAGE> {
	protected ObjectReader<IMAGE> reader;

	public ReadableImageDataset(ObjectReader<IMAGE> reader) {
		this.reader = reader;
	}

	@Override
	public IMAGE getRandomInstance() {
		return getInstance((int) (Math.random() * size()));
	}
}
