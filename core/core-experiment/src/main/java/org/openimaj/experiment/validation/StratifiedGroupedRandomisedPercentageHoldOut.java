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

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;

/**
 * Stratified Hold-Out validation for grouped data that selects a percentage of
 * the original data to use for training, and the remainder to use for
 * validation. The splitting of data is performed per group to ensure that the
 * relative group sizes remain (approximately) constant.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <KEY>
 *            Type of groups
 * @param <INSTANCE>
 *            Type of the instances in the dataset
 */
public class StratifiedGroupedRandomisedPercentageHoldOut<KEY, INSTANCE>
		extends
		DefaultValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>> {

	/**
	 * Construct with the given dataset and percentage of training data (0..1).
	 * 
	 * @param percentageTraining
	 *            percentage of the dataset to use for training
	 * @param dataset
	 *            the dataset
	 */
	public StratifiedGroupedRandomisedPercentageHoldOut(
			double percentageTraining,
			GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> dataset) {
		if (percentageTraining < 0 || percentageTraining > 1)
			throw new IllegalArgumentException(
					"percentage of training instances must be between 0 and 1");

		this.training = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>();
		this.validation = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>();

		Map<KEY, ListDataset<INSTANCE>> trainMap = ((MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>) training)
				.getMap();
		Map<KEY, ListDataset<INSTANCE>> validMap = ((MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>) validation)
				.getMap();

		for (KEY key : dataset.getGroups()) {
			RandomisedPercentageHoldOut<INSTANCE> ho = new RandomisedPercentageHoldOut<INSTANCE>(
					percentageTraining, dataset.getInstances(key));

			trainMap.put(key, ho.training);
			validMap.put(key, ho.validation);
		}
	}
}
