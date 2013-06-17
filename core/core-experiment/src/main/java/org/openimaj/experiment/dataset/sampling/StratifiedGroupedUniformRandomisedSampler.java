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

import java.util.List;
import java.util.Map;

import org.openimaj.data.RandomData;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.util.list.AcceptingListView;
import org.openimaj.util.list.SkippingListView;

/**
 * A stratified uniformly random sampling scheme for grouped datasets. Both
 * sampling with and without replacement are supported. The sampler returns a
 * dataset that selects a predefined fraction of the input data. Specifically,
 * the given percentage of data is selected from each group independently, thus
 * ensuring that the distribution of relative group sizes before and after
 * sampling remains (approximately) constant.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <KEY>
 *            Type of groups
 * @param <INSTANCE>
 *            Type of instances
 */
public class StratifiedGroupedUniformRandomisedSampler<KEY, INSTANCE>
		implements
		Sampler<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>
{
	private boolean withReplacement = false;

	// this is overloaded to hold either a percentage or number of instances.
	// Percentages are stored in the range 0..1; numbers are stored as -number.
	private double percentage;

	/**
	 * Construct a {@link StratifiedGroupedUniformRandomisedSampler}
	 * with the given percentage of instances to select. By default, the
	 * sampling is without replacement (i.e. an instance can only be selected
	 * once).
	 * 
	 * @param percentage
	 *            percentage of instances to select
	 */
	public StratifiedGroupedUniformRandomisedSampler(double percentage) {
		if (percentage < 0 || percentage > 1)
			throw new IllegalArgumentException("percentage of sample instances must be between 0 and 1");

		this.percentage = percentage;
	}

	/**
	 * Construct a {@link StratifiedGroupedUniformRandomisedSampler}
	 * with the given percentage of instances to select, using with
	 * with-replacement or without-replacement sampling.
	 * 
	 * @param percentage
	 *            percentage of instances to select
	 * @param withReplacement
	 *            should the sampling be performed with replacement (true); or
	 *            without replacement (false).
	 */
	public StratifiedGroupedUniformRandomisedSampler(
			double percentage, boolean withReplacement)
	{
		this(percentage);
		this.withReplacement = withReplacement;
	}

	/**
	 * Construct a {@link StratifiedGroupedUniformRandomisedSampler}
	 * with the given number of instances to select. By default, the sampling is
	 * without replacement (i.e. an instance can only be selected once).
	 * 
	 * @param number
	 *            number of instances to select
	 */
	public StratifiedGroupedUniformRandomisedSampler(int number) {
		if (number < 1)
			throw new IllegalArgumentException("number of sample instances must be greater than 1");

		this.percentage = -number;
	}

	/**
	 * Construct a {@link StratifiedGroupedUniformRandomisedSampler}
	 * with the given number of instances to select, using with with-replacement
	 * or without-replacement sampling.
	 * 
	 * @param number
	 *            number of instances to select
	 * @param withReplacement
	 *            should the sampling be performed with replacement (true); or
	 *            without replacement (false).
	 */
	public StratifiedGroupedUniformRandomisedSampler(
			int number, boolean withReplacement)
	{
		this(number);
		this.withReplacement = withReplacement;
	}

	@Override
	public GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> sample(
			GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> dataset)
	{
		final MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> sample = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>();
		final Map<KEY, ListDataset<INSTANCE>> map = sample.getMap();

		for (final KEY key : dataset.getGroups()) {
			final List<INSTANCE> list = DatasetAdaptors.asList(dataset
					.getInstances(key));
			final int size = list.size();

			final boolean skip;
			final int N;
			if (percentage >= 0) {
				// if we want more than 50%, it's better to select 1-percentage
				// indexes to skip
				skip = percentage > 0.5;
				final double per = skip ? 1.0 - percentage : percentage;

				N = (int) Math.round(size * per);
			} else {
				N = (int) -percentage;
				skip = N > (size / 2);
			}

			int[] selectedIds;
			if (withReplacement) {
				selectedIds = RandomData.getRandomIntArray(N, 0, size);
			} else {
				selectedIds = RandomData.getUniqueRandomInts(N, 0, size);
			}

			if (!skip) {
				map.put(key, new ListBackedDataset<INSTANCE>(
						new AcceptingListView<INSTANCE>(list, selectedIds)));
			} else {
				map.put(key, new ListBackedDataset<INSTANCE>(
						new SkippingListView<INSTANCE>(list, selectedIds)));
			}
		}

		return sample;
	}
}
