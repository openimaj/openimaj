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

import java.util.Collection;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;

/**
 * Sampler that samples whole groups from a {@link GroupedDataset}. Groups are
 * chosen by selecting pre-determined keys.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <KEY>
 *            Type of groups
 * @param <INSTANCE>
 *            Type of instances
 */
public class NamedGroupSampler<KEY, INSTANCE>
		implements
		Sampler<GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE>>
{
	Collection<KEY> keys;

	/**
	 * Construct the sample to extract the given groups.
	 * 
	 * @param keys
	 *            the group keys to sample.
	 */
	public NamedGroupSampler(Collection<KEY> keys) {
		this.keys = keys;
	}

	@Override
	public GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> sample(
			GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset)
	{
		final MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> sample = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>();

		for (final KEY key : keys) {
			sample.add(key, dataset.get(key));
		}

		return sample;
	}

	/**
	 * Sample a dataset by selecting only the given group keys.
	 * 
	 * @param dataset
	 *            the dataset to sample
	 * @param keys
	 *            the group keys to sample.
	 * @return the sampled dataset
	 */
	public static <KEY, INSTANCE> GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> sample(
			GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset, Collection<KEY> keys)
	{
		return new NamedGroupSampler<KEY, INSTANCE>(keys).sample(dataset);
	}
}
