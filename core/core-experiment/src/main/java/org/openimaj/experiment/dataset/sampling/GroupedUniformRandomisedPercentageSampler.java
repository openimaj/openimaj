package org.openimaj.experiment.dataset.sampling;

import java.util.Map;

import org.openimaj.data.RandomData;
import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapBackedDataset;
import org.openimaj.util.pair.IndependentPair;

/**
 * A uniformly random sampling scheme for grouped datasets. Both sampling with
 * and without replacement are supported. The sampler returns a dataset that
 * selects a predefined fraction of the input data. No attempt is made to ensure
 * that the distribution across groups is maintained (see
 * {@link StratifiedGroupedUniformRandomisedPercentageSampler} to achieve that).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <KEY>
 *            Type of groups
 * @param <INSTANCE>
 *            Type of instances
 */
public class GroupedUniformRandomisedPercentageSampler<KEY, INSTANCE>
implements
	Sampler<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>> 
{
	private boolean withReplacement = false;
	private double percentage;

	/**
	 * Construct a {@link GroupedUniformRandomisedPercentageSampler} with the given
	 * percentage of instances to select. By default, the sampling is without
	 * replacement (i.e. an instance can only be selected once).
	 * 
	 * @param percentage
	 *            percentage of instances to select
	 */
	public GroupedUniformRandomisedPercentageSampler(double percentage) {
		if (percentage < 0 || percentage > 1)
			throw new IllegalArgumentException("percentage of sample instances must be between 0 and 1");
		
		this.percentage = percentage;
	}

	/**
	 * Construct a {@link GroupedUniformRandomisedPercentageSampler} with the given
	 * percentage of instances to select, using with with-replacement or
	 * without-replacement sampling.
	 * 
	 * @param percentage
	 *            percentage of instances to select
	 * @param withReplacement
	 *            should the sampling be performed with replacement (true); or
	 *            without replacement (false).
	 */
	public GroupedUniformRandomisedPercentageSampler(double percentage,
			boolean withReplacement) {
		this(percentage);
		this.withReplacement = withReplacement;
	}

	@Override
	public GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> sample(
			GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> dataset) {
		final int N = (int) Math.round(dataset.size() * percentage);

		int[] selectedIds;
		if (withReplacement) {
			selectedIds = RandomData.getRandomIntArray(N, 0, dataset.size());
		} else {
			selectedIds = RandomData.getUniqueRandomInts(N, 0, dataset.size());
		}

		MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> sample = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>();
		Map<KEY, ListDataset<INSTANCE>> map = sample.getMap();

		for (int i = 0; i < N; i++) {
			IndependentPair<KEY, INSTANCE> p = select(selectedIds[i], dataset);

			ListBackedDataset<INSTANCE> lbd = (ListBackedDataset<INSTANCE>) map
					.get(p.firstObject());
			if (lbd == null)
				map.put(p.firstObject(),
						lbd = new ListBackedDataset<INSTANCE>());
			lbd.add(p.getSecondObject());
		}

		return sample;
	}

	private IndependentPair<KEY, INSTANCE> select(int idx,
			GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> dataset) {
		for (KEY k : dataset.getGroups()) {
			ListDataset<INSTANCE> instances = dataset.getInstances(k);
			int sz = instances.size();

			if (idx < sz) {
				return new IndependentPair<KEY, INSTANCE>(k,
						instances.getInstance(idx));
			}
			idx -= sz;
		}

		return null;
	}
}
