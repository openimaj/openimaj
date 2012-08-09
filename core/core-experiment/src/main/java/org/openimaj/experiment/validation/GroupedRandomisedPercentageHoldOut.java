package org.openimaj.experiment.validation;

import java.util.Map;

import org.openimaj.data.RandomData;
import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapBackedDataset;
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
		DefaultValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>> {
	/**
	 * Construct with the given dataset and percentage of training data (0..1).
	 * 
	 * @param percentageTraining
	 *            percentage of the dataset to use for training
	 * @param dataset
	 *            the dataset
	 */
	public GroupedRandomisedPercentageHoldOut(double percentageTraining,
			GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> dataset) {
		if (percentageTraining < 0 || percentageTraining > 1)
			throw new IllegalArgumentException(
					"percentage of training instances must be between 0 and 1");

		int size = dataset.size();
		int[] indices = RandomData.getUniqueRandomInts(size, 0, size);
		int nTrain = (int) (percentageTraining * size);

		this.training = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>();
		Map<KEY, ListDataset<INSTANCE>> trainMap = ((MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>) training)
				.getMap();
		for (int i = 0; i < nTrain; i++) {
			IndependentPair<KEY, INSTANCE> p = select(indices[i], dataset);

			ListBackedDataset<INSTANCE> lmd = (ListBackedDataset<INSTANCE>) trainMap
					.get(p.firstObject());
			if (lmd == null)
				trainMap.put(p.firstObject(),
						lmd = new ListBackedDataset<INSTANCE>());
			lmd.add(p.getSecondObject());
		}

		this.validation = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>();
		Map<KEY, ListDataset<INSTANCE>> validMap = ((MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>) validation)
				.getMap();
		for (int i = nTrain; i < size; i++) {
			IndependentPair<KEY, INSTANCE> p = select(indices[i], dataset);

			ListBackedDataset<INSTANCE> lmd = (ListBackedDataset<INSTANCE>) validMap
					.get(p.firstObject());
			if (lmd == null)
				validMap.put(p.firstObject(),
						lmd = new ListBackedDataset<INSTANCE>());
			lmd.add(p.getSecondObject());
		}
	}

	private IndependentPair<KEY, INSTANCE> select(int idx,
			GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> dataset) {
		for (KEY k : dataset.getGroups()) {
			ListDataset<INSTANCE> instances = dataset.getInstances(k);
			int sz = instances.size();

			if (idx < sz) {
				return new IndependentPair<KEY, INSTANCE>(k,
						instances.getInstance(idx));
			}
			idx -= sz;
		}

		return null;
	}
}
