package org.openimaj.experiment.validation;

import java.util.Map;

import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapBackedDataset;

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
