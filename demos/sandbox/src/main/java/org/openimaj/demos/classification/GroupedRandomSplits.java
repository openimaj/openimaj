package org.openimaj.demos.classification;

import java.util.Iterator;
import java.util.Map.Entry;

import org.openimaj.data.RandomData;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.experiment.dataset.split.TestSplitProvider;
import org.openimaj.experiment.dataset.split.TrainSplitProvider;
import org.openimaj.experiment.validation.ValidationData;
import org.openimaj.experiment.validation.cross.CrossValidationIterable;

public class GroupedRandomSplits<KEY, INSTANCE>
		implements
		TrainSplitProvider<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>,
		TestSplitProvider<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>
{
	private GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset;
	private GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> trainingSplit;
	private GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> testingSplit;
	private int numTraining;
	private int numTesting;

	public GroupedRandomSplits(GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset, int numTraining,
			int numTesting)
	{
		this.dataset = dataset;
		this.numTraining = numTraining;
		this.numTesting = numTesting;

		recompute();
	}

	public void recompute() {
		trainingSplit = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>();
		testingSplit = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>();

		for (final Entry<KEY, ? extends ListDataset<INSTANCE>> e : dataset.entrySet()) {
			final KEY key = e.getKey();
			final ListDataset<INSTANCE> allData = e.getValue();

			if (allData.size() < numTraining + 1)
				throw new RuntimeException("Too many training examples; none would be available for testing.");

			final int[] ids = RandomData.getUniqueRandomInts(Math.min(numTraining + numTesting, allData.size()), 0,
					allData.size());

			final ListDataset<INSTANCE> train = new ListBackedDataset<INSTANCE>();
			for (int i = 0; i < numTraining; i++) {
				train.add(allData.get(ids[i]));
			}
			trainingSplit.put(key, train);

			final ListDataset<INSTANCE> test = new ListBackedDataset<INSTANCE>();
			for (int i = numTraining; i < ids.length; i++) {
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

	/**
	 * Create a {@link CrossValidationIterable} from the dataset.
	 * 
	 * @param data
	 *            the dataset
	 * @return the iterable
	 */
	public CrossValidationIterable<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>> createIterable(
			final int numIterations)
	{
		return new CrossValidationIterable<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>() {

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
						recompute();
						current++;

						return new ValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>() {

							@Override
							public GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> getTrainingDataset() {
								return GroupedRandomSplits.this.getTrainingDataset();
							}

							@Override
							public GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> getValidationDataset() {
								return GroupedRandomSplits.this.getTestDataset();
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
