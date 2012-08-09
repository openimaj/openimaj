package org.openimaj.experiment.dataset.sampling;

import java.util.List;

import org.openimaj.data.RandomData;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.util.list.AcceptingListView;
import org.openimaj.util.list.SkippingListView;

/**
 * A uniformly random sampling scheme for {@link ListDataset}s. Both
 * sampling with and without replacement are supported. The sampler
 * returns a "view" on top of the input dataset that selects a predefined
 * fraction of the data. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <INSTANCE> Type of instances
 */
public class UniformRandomisedPercentageSampler<INSTANCE> implements Sampler<ListDataset<INSTANCE>> {
	private boolean withReplacement = false;
	private double percentage;
	
	/**
	 * Construct a {@link UniformRandomisedPercentageSampler} with the
	 * given percentage of instances to select. By default, the sampling
	 * is without replacement (i.e. an instance can only be selected once).
	 * 
	 * @param percentage percentage of instances to select
	 */
	public UniformRandomisedPercentageSampler(double percentage) {
		if (percentage < 0 || percentage > 1)
			throw new IllegalArgumentException("percentage of sample instances must be between 0 and 1");
		
		this.percentage = percentage;
	}
	
	/**
	 * Construct a {@link UniformRandomisedPercentageSampler} with the
	 * given percentage of instances to select, using with with-replacement
	 * or without-replacement sampling.
	 * 
	 * @param percentage percentage of instances to select
	 * @param withReplacement should the sampling be performed with 
	 * 			replacement (true); or without replacement (false).
	 */
	public UniformRandomisedPercentageSampler(double percentage, boolean withReplacement) {
		this(percentage);
		this.withReplacement = withReplacement;
	}
	
	@Override
	public ListDataset<INSTANCE> sample(ListDataset<INSTANCE> dataset) {
		//if we want more than 50%, it's better to select 1-percentage
		//indexes to skip
		final boolean skip = percentage > 0.5;
		final double per = skip ? 1.0 - percentage : percentage;
		
		final int N = (int) Math.round(dataset.size() * per);
		
		int[] selectedIds;
		if (withReplacement) {
			selectedIds = RandomData.getRandomIntArray(N, 0, dataset.size());
		} else {
			selectedIds = RandomData.getUniqueRandomInts(N, 0, dataset.size());
		}
		
		List<INSTANCE> listView = DatasetAdaptors.asList(dataset);
		
		if (!skip) {
			return new ListBackedDataset<INSTANCE>(new AcceptingListView<INSTANCE>(listView, selectedIds));
		}
		
		return new ListBackedDataset<INSTANCE>(new SkippingListView<INSTANCE>(listView, selectedIds));
	}
}
