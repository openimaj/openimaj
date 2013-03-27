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

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.openimaj.util.pair.IntObjectPair;

/**
 * K-Fold Cross-Validation on grouped datasets.
 * <p>
 * All the instances are split into k subsets. The validation data in each
 * iteration is one of the subsets, whilst the training data is the remaindering
 * subsets. The subsets are not guaranteed to have any particular balance of
 * groups as the splitting is completely random; however if there is the same
 * number of instances per group, then the subsets should be balanced on
 * average. A particular fold <b>could</b> potentially have no training or
 * validation data for a particular class.
 * <p>
 * Setting the number of splits to be equal to the number of total instances is
 * equivalent to LOOCV. If LOOCV is the aim, the {@link GroupedLeaveOneOut}
 * class is a more efficient implementation than this class.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <KEY>
 *            Type of groups
 * @param <INSTANCE>
 *            Type of instances
 */
public class GroupedKFold<KEY, INSTANCE> implements CrossValidator<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>> {
	private class GroupedKFoldIterable
			implements
			CrossValidationIterable<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>
	{
		private GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset;
		private Map<KEY, int[][]> subsetIndices = new HashMap<KEY, int[][]>();
		private int numFolds;

		/**
		 * Construct the {@link GroupedKFoldIterable} with the given dataset and
		 * number of folds.
		 * 
		 * @param dataset
		 *            the dataset
		 * @param k
		 *            the target number of folds.
		 */
		public GroupedKFoldIterable(GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset, int k) {
			if (k > dataset.numInstances())
				throw new IllegalArgumentException(
						"The number of folds must be less than the number of items in the dataset");

			if (k <= 0)
				throw new IllegalArgumentException("The number of folds must be at least one");

			this.dataset = dataset;
			this.numFolds = k;

			final int[] allIndices = RandomData.getUniqueRandomInts(dataset.numInstances(), 0, dataset.numInstances());
			final int[][] flatSubsetIndices = new int[k][];

			final int splitSize = dataset.numInstances() / k;
			for (int i = 0; i < k - 1; i++) {
				flatSubsetIndices[i] = Arrays.copyOfRange(allIndices, splitSize * i, splitSize * (i + 1));
			}
			flatSubsetIndices[k - 1] = Arrays.copyOfRange(allIndices, splitSize * (k - 1), allIndices.length);

			final ArrayList<KEY> groups = new ArrayList<KEY>(dataset.getGroups());

			for (final KEY key : groups) {
				subsetIndices.put(key, new int[k][]);
			}

			for (int i = 0; i < flatSubsetIndices.length; i++) {
				final Map<KEY, TIntArrayList> tmp = new HashMap<KEY, TIntArrayList>();

				for (final int flatIdx : flatSubsetIndices[i]) {
					final IntObjectPair<KEY> idx = computeIndex(groups, flatIdx);

					TIntArrayList list = tmp.get(idx.second);
					if (list == null)
						tmp.put(idx.second, list = new TIntArrayList());
					list.add(idx.first);
				}

				for (final Entry<KEY, TIntArrayList> kv : tmp.entrySet()) {
					subsetIndices.get(kv.getKey())[i] = kv.getValue().toArray();
				}
			}
		}

		private IntObjectPair<KEY> computeIndex(ArrayList<KEY> groups, int flatIdx) {
			int count = 0;

			for (final KEY group : groups) {
				final ListDataset<INSTANCE> instances = dataset.getInstances(group);
				final int size = instances.size();

				if (count + size <= flatIdx) {
					count += size;
				} else {
					return new IntObjectPair<KEY>(flatIdx - count, group);
				}
			}

			throw new RuntimeException("Index not found");
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
	 * Construct the {@link GroupedKFold} with the given number of folds.
	 * 
	 * @param k
	 *            the target number of folds.
	 */
	public GroupedKFold(int k) {
		this.k = k;
	}

	@Override
	public CrossValidationIterable<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>> createIterable(
			GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> data)
	{
		return new GroupedKFoldIterable(data, k);
	}

	@Override
	public String toString() {
		return k + "-Fold Cross-Validation for grouped datasets";
	}
}
