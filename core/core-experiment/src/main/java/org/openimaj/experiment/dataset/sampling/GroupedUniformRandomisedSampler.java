/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.experiment.dataset.sampling;

import java.util.Map;

import org.openimaj.data.RandomData;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.util.pair.IndependentPair;

/**
 * A uniformly random sampling scheme for grouped datasets. Both sampling with
 * and without replacement are supported. The sampler returns a dataset that
 * selects a predefined fraction of the input data. No attempt is made to ensure
 * that the distribution across groups is maintained (see
 * {@link StratifiedGroupedUniformRandomisedSampler} to achieve that).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <KEY>
 *            Type of groups
 * @param <INSTANCE>
 *            Type of instances
 */
public class GroupedUniformRandomisedSampler<KEY, INSTANCE>
		implements
		Sampler<GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE>>
{
	private boolean withReplacement = false;

	// this is overloaded to hold either a percentage or number of instances.
	// Percentages are stored in the range 0..1; numbers are stored as -number.
	private double percentage;

	/**
	 * Construct a {@link GroupedUniformRandomisedSampler} with the given
	 * percentage of instances to select. By default, the sampling is without
	 * replacement (i.e. an instance can only be selected once).
	 * 
	 * @param percentage
	 *            percentage of instances to select
	 */
	public GroupedUniformRandomisedSampler(double percentage) {
		if (percentage < 0 || percentage > 1)
			throw new IllegalArgumentException("percentage of sample instances must be between 0 and 1");

		this.percentage = percentage;
	}

	/**
	 * Construct a {@link GroupedUniformRandomisedSampler} with the given
	 * percentage of instances to select, using with with-replacement or
	 * without-replacement sampling.
	 * 
	 * @param percentage
	 *            percentage of instances to select
	 * @param withReplacement
	 *            should the sampling be performed with replacement (true); or
	 *            without replacement (false).
	 */
	public GroupedUniformRandomisedSampler(double percentage,
			boolean withReplacement)
	{
		this(percentage);
		this.withReplacement = withReplacement;
	}

	/**
	 * Construct a {@link GroupedUniformRandomisedSampler} with the given number
	 * of instances to select. By default, the sampling is without replacement
	 * (i.e. an instance can only be selected once).
	 * 
	 * @param number
	 *            number of instances to select
	 */
	public GroupedUniformRandomisedSampler(int number) {
		if (number < 1)
			throw new IllegalArgumentException("number of sample instances must be greater than 0");

		this.percentage = -number;
	}

	/**
	 * Construct a {@link GroupedUniformRandomisedSampler} with the given number
	 * of instances to select, using with with-replacement or
	 * without-replacement sampling.
	 * 
	 * @param number
	 *            number of instances to select
	 * @param withReplacement
	 *            should the sampling be performed with replacement (true); or
	 *            without replacement (false).
	 */
	public GroupedUniformRandomisedSampler(int number,
			boolean withReplacement)
	{
		this(number);
		this.withReplacement = withReplacement;
	}

	@Override
	public GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> sample(
			GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset)
	{
		final int N;
		if (percentage >= 0) {
			N = (int) Math.round(dataset.numInstances() * percentage);
		} else {
			N = (int) -percentage;
		}

		int[] selectedIds;
		if (withReplacement) {
			selectedIds = RandomData.getRandomIntArray(N, 0, dataset.numInstances());
		} else {
			selectedIds = RandomData.getUniqueRandomInts(N, 0, dataset.numInstances());
		}

		final MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> sample = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>();
		final Map<KEY, ListDataset<INSTANCE>> map = sample.getMap();

		for (int i = 0; i < N; i++) {
			final IndependentPair<KEY, INSTANCE> p = select(selectedIds[i], dataset);

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
			GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset)
	{
		for (final KEY k : dataset.getGroups()) {
			final ListDataset<INSTANCE> instances = dataset.getInstances(k);
			final int sz = instances.size();

			if (idx < sz) {
				return new IndependentPair<KEY, INSTANCE>(k,
						instances.getInstance(idx));
			}
			idx -= sz;
		}

		return null;
	}

	/**
	 * Sample a dataset with the given percentage of instances to select. By
	 * default, the sampling is without replacement (i.e. an instance can only
	 * be selected once).
	 * 
	 * @param dataset
	 *            the dataset to sample
	 * @param percentage
	 *            percentage of instances to select
	 * @return the sampled dataset
	 */
	public static <KEY, INSTANCE> GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> sample(
			GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset, double percentage)
	{
		return new GroupedUniformRandomisedSampler<KEY, INSTANCE>(percentage).sample(dataset);
	}

	/**
	 * Sample a dataset with the given percentage of instances to select, using
	 * with with-replacement or without-replacement sampling.
	 * 
	 * @param dataset
	 *            the dataset to sample
	 * @param percentage
	 *            percentage of instances to select
	 * @param withReplacement
	 *            should the sampling be performed with replacement (true); or
	 *            without replacement (false).
	 * @return the sampled dataset
	 */
	public static <KEY, INSTANCE> GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> sample(
			GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset, double percentage,
			boolean withReplacement)
	{
		return new GroupedUniformRandomisedSampler<KEY, INSTANCE>(percentage, withReplacement).sample(dataset);
	}

	/**
	 * Sample a dataset with the given number of instances to select. By
	 * default, the sampling is without replacement (i.e. an instance can only
	 * be selected once).
	 * 
	 * @param dataset
	 *            the dataset to sample
	 * @param number
	 *            number of instances to select
	 * @return the sampled dataset
	 */
	public static <KEY, INSTANCE> GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> sample(
			GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset, int number)
	{
		return new GroupedUniformRandomisedSampler<KEY, INSTANCE>(number).sample(dataset);
	}

	/**
	 * Sample a dataset with the given number of instances to select, using with
	 * with-replacement or without-replacement sampling.
	 * 
	 * @param dataset
	 *            the dataset to sample
	 * @param number
	 *            number of instances to select
	 * @param withReplacement
	 *            should the sampling be performed with replacement (true); or
	 *            without replacement (false).
	 * @return the sampled dataset
	 */
	public static <KEY, INSTANCE> GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> sample(
			GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset, int number,
			boolean withReplacement)
	{
		return new GroupedUniformRandomisedSampler<KEY, INSTANCE>(number, withReplacement).sample(dataset);
	}
}
