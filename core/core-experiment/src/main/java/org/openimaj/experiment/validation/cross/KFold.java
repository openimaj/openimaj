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
import java.util.Iterator;
import java.util.List;

import org.openimaj.data.RandomData;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.experiment.validation.DefaultValidationData;
import org.openimaj.experiment.validation.ValidationData;
import org.openimaj.util.list.AcceptingListView;
import org.openimaj.util.list.SkippingListView;

/**
 * K-Fold cross validation for {@link ListDataset}s. The data is broken
 * into K approximately equally sized non-overlapping randomised subsets. 
 * On each iteration, one subset is picked as the validation data and the 
 * remaining subsets are combined to make the training data. The number of
 * iterations is equal to the number of subsets.
 * <p>
 * If the number of subsets is equal to the number of instances, then
 * the K-Fold Cross Validation scheme becomes equivalent to the 
 * LOOCV scheme. The implementation of LOOCV in the {@link LeaveOneOut}
 * class is considerably more memory efficient than using this class
 * however.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <INSTANCE> Type of instances
 */
public class KFold<INSTANCE> implements CrossValidator<ListDataset<INSTANCE>> {
	private class KFoldIterable implements CrossValidationIterable<ListDataset<INSTANCE>> {
		private List<INSTANCE> listView;
		private int[][] subsetIndices;

		/**
		 * Construct with the given dataset and number of folds.
		 * 
		 * @param dataset the dataset
		 * @param k the number of folds.
		 */
		public KFoldIterable(ListDataset<INSTANCE> dataset, int k) {
			if (k > dataset.size())
				throw new IllegalArgumentException("The number of folds must be less than the number of items in the dataset");

			if (k <= 0)
				throw new IllegalArgumentException("The number of folds must be at least one");

			this.listView = DatasetAdaptors.asList(dataset);

			int[] allIndices = RandomData.getUniqueRandomInts(dataset.size(), 0, dataset.size());
			subsetIndices = new int[k][];

			int splitSize = dataset.size() / k;
			for (int i=0; i<k-1; i++) { 
				subsetIndices[i] = Arrays.copyOfRange(allIndices, splitSize * i, splitSize * (i + 1));
			}
			subsetIndices[k-1] = Arrays.copyOfRange(allIndices, splitSize * (k - 1), allIndices.length);
		}

		/**
		 * Get the number of iterations that the {@link Iterator}
		 * returned by {@link #iterator()} will perform.
		 * 
		 * @return the number of iterations that will be performed
		 */
		@Override
		public int numberIterations() {
			return subsetIndices.length;
		}

		@Override
		public Iterator<ValidationData<ListDataset<INSTANCE>>> iterator() {
			return new Iterator<ValidationData<ListDataset<INSTANCE>>>() {
				int validationSubset = 0;

				@Override
				public boolean hasNext() {
					return validationSubset < subsetIndices.length;
				}

				@Override
				public ValidationData<ListDataset<INSTANCE>> next() {
					ListDataset<INSTANCE> training = new ListBackedDataset<INSTANCE>(new SkippingListView<INSTANCE>(listView, subsetIndices[validationSubset]));
					ListDataset<INSTANCE> validation = new ListBackedDataset<INSTANCE>(new AcceptingListView<INSTANCE>(listView, subsetIndices[validationSubset]));

					validationSubset++;

					return new DefaultValidationData<ListDataset<INSTANCE>>(training, validation);
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
	 * Construct with the given number of folds.
	 * 
	 * @param k the number of folds.
	 */
	public KFold(int k) {
		this.k = k;
	}
	
	@Override
	public CrossValidationIterable<ListDataset<INSTANCE>> createIterable(ListDataset<INSTANCE> data) {
		return new KFoldIterable(data, k);
	}
	
	@Override
	public String toString() {
		return k +"-Fold Cross-Validation";
	}
}