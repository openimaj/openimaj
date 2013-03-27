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
package org.openimaj.experiment.validation.cross;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.data.RandomData;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.experiment.validation.DefaultValidationData;
import org.openimaj.experiment.validation.ValidationData;
import org.openimaj.util.list.AcceptingListView;
import org.openimaj.util.list.SkippingListView;

/**
 * Stratified K-Fold Cross-Validation on grouped datasets.
 * <p>
 * This implementation randomly splits the data in each group into K
 * non-overlapping subsets. The number of folds, K, is set at the size of the
 * smallest group if it is bigger; this ensures that each fold will contain at
 * least one training and validation example for each group, and that the
 * relative distribution of instances per group for each fold is approximately
 * the same as for the full dataset.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <KEY>
 *            Type of groups
 * @param <INSTANCE>
 *            Type of instances
 */
public class StratifiedGroupedKFold<KEY, INSTANCE>
		implements
			CrossValidator<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>
{
	private class StratifiedGroupedKFoldIterable
			implements
				CrossValidationIterable<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>
	{
		private GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset;
		private Map<KEY, int[][]> subsetIndices = new HashMap<KEY, int[][]>();
		private int numFolds;

		/**
		 * Construct a {@link StratifiedGroupedKFoldIterable} with the given
		 * dataset and target number of folds, K. If a group in the dataset has
		 * fewer than K instances, then the number of folds will be reduced to
		 * the number of instances.
		 * 
		 * @param dataset
		 *            the dataset
		 * @param k
		 *            the target number of folds.
		 */
		public StratifiedGroupedKFoldIterable(GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset,
				int k)
		{
			if (k > dataset.numInstances())
				throw new IllegalArgumentException(
						"The number of folds must be less than the number of items in the dataset");

			if (k <= 0)
				throw new IllegalArgumentException("The number of folds must be at least one");

			this.dataset = dataset;

			final Set<KEY> keys = dataset.getGroups();

			// compute min group size
			int minGroupSize = Integer.MAX_VALUE;
			for (final KEY group : keys) {
				final int instancesSize = dataset.getInstances(group).size();
				if (instancesSize < minGroupSize)
					minGroupSize = instancesSize;
			}

			// set the num folds
			if (k < minGroupSize)
				this.numFolds = k;
			else
				this.numFolds = minGroupSize;

			for (final KEY group : keys) {
				final int keySize = dataset.getInstances(group).size();

				final int[] allKeyIndices = RandomData.getUniqueRandomInts(keySize, 0, keySize);

				subsetIndices.put(group, new int[numFolds][]);
				final int[][] si = subsetIndices.get(group);

				final int splitSize = keySize / numFolds;
				for (int i = 0; i < numFolds - 1; i++) {
					si[i] = Arrays.copyOfRange(allKeyIndices, splitSize * i, splitSize * (i + 1));
				}
				si[numFolds - 1] = Arrays.copyOfRange(allKeyIndices, splitSize * (numFolds - 1), allKeyIndices.length);
			}
		}

		/**
		 * Get the number of iterations that the {@link Iterator} returned by
		 * {@link #iterator()} will perform.
		 * 
		 * @return the number of iterations that will be performed
		 */
		@Override
		public int numberIterations() {
			return numFolds;
		}

		@Override
		public Iterator<ValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>> iterator() {
			return new Iterator<ValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>>() {
				int validationSubset = 0;

				@Override
				public boolean hasNext() {
					return validationSubset < numFolds;
				}

				@Override
				public ValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>> next() {
					final Map<KEY, ListDataset<INSTANCE>> train = new HashMap<KEY, ListDataset<INSTANCE>>();
					final Map<KEY, ListDataset<INSTANCE>> valid = new HashMap<KEY, ListDataset<INSTANCE>>();

					for (final KEY group : subsetIndices.keySet()) {
						final int[][] si = subsetIndices.get(group);

						final List<INSTANCE> keyData = DatasetAdaptors.asList(dataset.getInstances(group));

						train.put(group, new ListBackedDataset<INSTANCE>(new SkippingListView<INSTANCE>(keyData,
								si[validationSubset])));
						valid.put(group, new ListBackedDataset<INSTANCE>(new AcceptingListView<INSTANCE>(keyData,
								si[validationSubset])));
					}

					final MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> cvTrain = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>(
							train);
					final MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> cvValid = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>(
							valid);

					validationSubset++;

					return new DefaultValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>(cvTrain,
							cvValid);
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	}

	private int k;

	/**
	 * Construct a {@link StratifiedGroupedKFold} with the given target number
	 * of folds, K. If a group in the dataset has fewer than K instances, then
	 * the number of folds will be reduced to the number of instances.
	 * 
	 * @param k
	 *            the target number of folds.
	 */
	public StratifiedGroupedKFold(int k) {
		this.k = k;
	}

	@Override
	public CrossValidationIterable<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>> createIterable(
			GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> data)
	{
		return new StratifiedGroupedKFoldIterable(data, k);
	}

	@Override
	public String toString() {
		return "Stratified " + k + "-Fold Cross-Validation for grouped datasets";
	}
}
