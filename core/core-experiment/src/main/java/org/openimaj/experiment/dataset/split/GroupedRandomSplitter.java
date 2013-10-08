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
package org.openimaj.experiment.dataset.split;

import java.util.Iterator;
import java.util.Map.Entry;

import org.openimaj.data.RandomData;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.experiment.validation.ValidationData;
import org.openimaj.experiment.validation.cross.CrossValidationIterable;

/**
 * This class splits a {@link GroupedDataset} into subsets for training,
 * validation and testing. The number of instances required for each subset can
 * be chosen independently. Instances are assigned to subsets randomly without
 * replacement within the groups.
 * <p>
 * The {@link GroupedRandomSplitter} class allows the splits to be recomputed at
 * any time. This makes it easy to generate new splits (for cross-validation for
 * example). There are static methods to simplify the generation of such data.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <KEY>
 *            Type of dataset class key
 * @param <INSTANCE>
 *            Type of instances in the dataset
 */
public class GroupedRandomSplitter<KEY, INSTANCE>
		implements
		TrainSplitProvider<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>,
		TestSplitProvider<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>,
		ValidateSplitProvider<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>
{
	private GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset;
	private GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> trainingSplit;
	private GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> validationSplit;
	private GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> testingSplit;
	private int numTraining;
	private int numValidation;
	private int numTesting;

	/**
	 * Construct the dataset splitter with the given target instance sizes for
	 * each group of the training, validation and testing data. The actual
	 * number of instances per subset and group will not necessarily be the
	 * specified number if there are not enough instances in the input dataset.
	 * Instances are assigned randomly with preference to the training set
	 * followed by the validation set. If, for example, you had 40 instances in
	 * a group of the input dataset and requested a training size of 20,
	 * validation size of 15 and testing size of 10, then your actual testing
	 * set would only have 5 instances rather than the 10 requested. If any
	 * subset will end up having no instances of a particular group available an
	 * exception will be thrown.
	 * 
	 * @param dataset
	 *            the dataset to split
	 * @param numTraining
	 *            the number of training instances per group
	 * @param numValidation
	 *            the number of validation instances per group
	 * @param numTesting
	 *            the number of testing instances per group
	 */
	public GroupedRandomSplitter(GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset, int numTraining,
			int numValidation,
			int numTesting)
	{
		this.dataset = dataset;
		this.numTraining = numTraining;
		this.numValidation = numValidation;
		this.numTesting = numTesting;

		recomputeSubsets();
	}

	/**
	 * Recompute the underlying splits of the training, validation and testing
	 * data by randomly picking new subsets of the input dataset given in the
	 * constructor.
	 */
	public void recomputeSubsets() {
		trainingSplit = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>();
		validationSplit = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>();
		testingSplit = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>();

		for (final Entry<KEY, ? extends ListDataset<INSTANCE>> e : dataset.entrySet()) {
			final KEY key = e.getKey();
			final ListDataset<INSTANCE> allData = e.getValue();

			if (allData.size() < numTraining + 1)
				throw new RuntimeException(
						"Too many training examples; none would be available for validation or testing.");

			if (allData.size() < numTraining + numValidation + 1)
				throw new RuntimeException(
						"Too many training and validation instances; none would be available for testing.");

			final int[] ids = RandomData.getUniqueRandomInts(
					Math.min(numTraining + numValidation + numTesting, allData.size()), 0,
					allData.size());

			final ListDataset<INSTANCE> train = new ListBackedDataset<INSTANCE>();
			for (int i = 0; i < numTraining; i++) {
				train.add(allData.get(ids[i]));
			}
			trainingSplit.put(key, train);

			final ListDataset<INSTANCE> valid = new ListBackedDataset<INSTANCE>();
			for (int i = numTraining; i < numTraining + numValidation; i++) {
				valid.add(allData.get(ids[i]));
			}
			validationSplit.put(key, valid);

			final ListDataset<INSTANCE> test = new ListBackedDataset<INSTANCE>();
			for (int i = numTraining + numValidation; i < ids.length; i++) {
				test.add(allData.get(ids[i]));
			}
			testingSplit.put(key, test);
		}
	}

	@Override
	public GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> getTestDataset() {
		return testingSplit;
	}

	@Override
	public GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> getTrainingDataset() {
		return trainingSplit;
	}

	@Override
	public GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> getValidationDataset() {
		return validationSplit;
	}

	/**
	 * Create a {@link CrossValidationIterable} from the dataset. Internally,
	 * this method creates a {@link GroupedRandomSplitter} to split the dataset
	 * into subsets of the requested size (with no test instances) and then
	 * produces an {@link CrossValidationIterable} that recomputes the subsets
	 * on each iteration through {@link #recomputeSubsets()}.
	 * 
	 * @param dataset
	 *            the dataset to split
	 * @param numTraining
	 *            the number of training instances per group
	 * @param numValidation
	 *            the number of validation instances per group
	 * @param numIterations
	 *            the number of cross-validation iterations to create
	 * @return the cross-validation datasets in the form of a
	 *         {@link CrossValidationIterable}
	 * 
	 * @param <KEY>
	 *            Type of dataset class key
	 * @param <INSTANCE>
	 *            Type of instances in the dataset
	 */
	public static <KEY, INSTANCE> CrossValidationIterable<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>
			createCrossValidationData(final GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset,
					final int numTraining, final int numValidation, final int numIterations)
	{
		return new CrossValidationIterable<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>() {
			private GroupedRandomSplitter<KEY, INSTANCE> splits = new GroupedRandomSplitter<KEY, INSTANCE>(dataset,
					numTraining, numValidation, 0);

			@Override
			public Iterator<ValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>> iterator() {
				return new Iterator<ValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>>() {
					int current = 0;

					@Override
					public boolean hasNext() {
						return current < numIterations;
					}

					@Override
					public ValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>> next() {
						splits.recomputeSubsets();
						current++;

						return new ValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>() {

							@Override
							public GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> getTrainingDataset() {
								return splits.getTrainingDataset();
							}

							@Override
							public GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> getValidationDataset() {
								return splits.getValidationDataset();
							}
						};
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException("Removal not supported");
					}
				};
			}

			@Override
			public int numberIterations() {
				return numIterations;
			}
		};
	}
}
