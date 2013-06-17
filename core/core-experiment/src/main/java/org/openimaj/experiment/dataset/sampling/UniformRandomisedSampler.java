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

import org.openimaj.data.RandomData;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.util.list.AcceptingListView;
import org.openimaj.util.list.SkippingListView;

/**
 * A uniformly random sampling scheme for {@link ListDataset}s. Both sampling
 * with and without replacement are supported. The sampler returns a "view" on
 * top of the input dataset that selects a predefined fraction of the data.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <INSTANCE>
 *            Type of instances
 */
public class UniformRandomisedSampler<INSTANCE> implements Sampler<ListDataset<INSTANCE>> {
	private boolean withReplacement = false;

	// this is overloaded to hold either a percentage or number of instances.
	// Percentages are stored in the range 0..1; numbers are stored as -number.
	private double percentage;

	/**
	 * Construct a {@link UniformRandomisedSampler} with the given percentage of
	 * instances to select. By default, the sampling is without replacement
	 * (i.e. an instance can only be selected once).
	 * 
	 * @param percentage
	 *            percentage of instances to select
	 */
	public UniformRandomisedSampler(double percentage) {
		if (percentage < 0 || percentage > 1)
			throw new IllegalArgumentException("percentage of sample instances must be between 0 and 1");

		this.percentage = percentage;
	}

	/**
	 * Construct a {@link UniformRandomisedSampler} with the given percentage of
	 * instances to select, using with with-replacement or without-replacement
	 * sampling.
	 * 
	 * @param percentage
	 *            percentage of instances to select
	 * @param withReplacement
	 *            should the sampling be performed with replacement (true); or
	 *            without replacement (false).
	 */
	public UniformRandomisedSampler(double percentage, boolean withReplacement) {
		this(percentage);
		this.withReplacement = withReplacement;
	}

	/**
	 * Construct a {@link UniformRandomisedSampler} with the given number of
	 * instances to select. By default, the sampling is without replacement
	 * (i.e. an instance can only be selected once).
	 * 
	 * @param number
	 *            number of instances to select
	 */
	public UniformRandomisedSampler(int number) {
		if (number < 1)
			throw new IllegalArgumentException("number of sample instances must be bigger than 0");

		this.percentage = -number;
	}

	/**
	 * Construct a {@link UniformRandomisedSampler} with the given number of
	 * instances to select, using with with-replacement or without-replacement
	 * sampling.
	 * 
	 * @param number
	 *            number of instances to select
	 * @param withReplacement
	 *            should the sampling be performed with replacement (true); or
	 *            without replacement (false).
	 */
	public UniformRandomisedSampler(int number, boolean withReplacement) {
		this(number);
		this.withReplacement = withReplacement;
	}

	@Override
	public ListDataset<INSTANCE> sample(ListDataset<INSTANCE> dataset) {
		final boolean skip;
		final int N;
		if (percentage >= 0) {
			// if we want more than 50%, it's better to select 1-percentage
			// indexes to skip
			skip = percentage > 0.5;
			final double per = skip ? 1.0 - percentage : percentage;

			N = (int) Math.round(dataset.size() * per);
		} else {
			N = (int) -percentage;
			skip = N > (dataset.size() / 2);
		}

		int[] selectedIds;
		if (withReplacement) {
			selectedIds = RandomData.getRandomIntArray(N, 0, dataset.size());
		} else {
			selectedIds = RandomData.getUniqueRandomInts(N, 0, dataset.size());
		}

		final List<INSTANCE> listView = DatasetAdaptors.asList(dataset);

		if (!skip) {
			return new ListBackedDataset<INSTANCE>(new AcceptingListView<INSTANCE>(listView, selectedIds));
		}

		return new ListBackedDataset<INSTANCE>(new SkippingListView<INSTANCE>(listView, selectedIds));
	}
}
