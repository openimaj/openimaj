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
package org.openimaj.experiment.validation;

import java.util.Map;

import org.openimaj.data.RandomData;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.util.pair.IndependentPair;

/**
 * Hold-Out validation for grouped data that selects a percentage of the
 * original data to use for training, and the remainder to use for validation.
 * No attempt is made to ensure that the distribution of relative group sizes is
 * constant. If this is important, use a
 * {@link StratifiedGroupedRandomisedPercentageHoldOut} instead.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <KEY>
 *            Type of groups
 * @param <INSTANCE>
 *            Type of the instances in the dataset
 */
public class GroupedRandomisedPercentageHoldOut<KEY, INSTANCE>
		extends
		DefaultValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>
{
	/**
	 * Construct with the given dataset and percentage of training data (0..1).
	 * 
	 * @param percentageTraining
	 *            percentage of the dataset to use for training
	 * @param dataset
	 *            the dataset
	 */
	public GroupedRandomisedPercentageHoldOut(double percentageTraining,
			GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> dataset)
	{
		if (percentageTraining < 0 || percentageTraining > 1)
			throw new IllegalArgumentException(
					"percentage of training instances must be between 0 and 1");

		final int size = dataset.numInstances();
		final int[] indices = RandomData.getUniqueRandomInts(size, 0, size);
		final int nTrain = (int) (percentageTraining * size);

		this.training = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>();
		final Map<KEY, ListDataset<INSTANCE>> trainMap = ((MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>) training)
				.getMap();
		for (int i = 0; i < nTrain; i++) {
			final IndependentPair<KEY, INSTANCE> p = select(indices[i], dataset);

			ListBackedDataset<INSTANCE> lmd = (ListBackedDataset<INSTANCE>) trainMap
					.get(p.firstObject());
			if (lmd == null)
				trainMap.put(p.firstObject(),
						lmd = new ListBackedDataset<INSTANCE>());
			lmd.add(p.getSecondObject());
		}

		this.validation = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>();
		final Map<KEY, ListDataset<INSTANCE>> validMap = ((MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>) validation)
				.getMap();
		for (int i = nTrain; i < size; i++) {
			final IndependentPair<KEY, INSTANCE> p = select(indices[i], dataset);

			ListBackedDataset<INSTANCE> lmd = (ListBackedDataset<INSTANCE>) validMap
					.get(p.firstObject());
			if (lmd == null)
				validMap.put(p.firstObject(),
						lmd = new ListBackedDataset<INSTANCE>());
			lmd.add(p.getSecondObject());
		}
	}

	private IndependentPair<KEY, INSTANCE> select(int idx,
			GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> dataset)
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
}
