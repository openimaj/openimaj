package org.openimaj.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openimaj.data.RandomData;

/**
 * Implementation of a {@link CollectionSampler} that performs uniform sampling.
 * Each sampled set is sampled without replacement (i.e. an item will only
 * appear once), however there is no guarantee that subsequent calls to
 * {@link #sample(int)} will return unique sets of samples.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            type of items in the collection being sampled
 */
public class UniformSampler<T> implements CollectionSampler<T> {
	private List<T> data;

	@Override
	public void setCollection(Collection<? extends T> collection) {
		this.data = new ArrayList<T>(collection);
	}

	@Override
	public List<T> sample(int nItems) {
		final int[] rints = RandomData.getUniqueRandomInts(nItems, 0, data.size());
		final List<T> out = new ArrayList<T>(nItems);

		for (final int i : rints) {
			out.add(data.get(i));
		}

		return out;
	}
}
