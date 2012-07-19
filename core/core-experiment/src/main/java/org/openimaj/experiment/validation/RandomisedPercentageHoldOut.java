package org.openimaj.experiment.validation;

import org.openimaj.data.RandomData;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.util.list.AcceptingListView;
import org.openimaj.util.list.SkippingListView;

/**
 * Hold-Out validation that selects a percentage of the original
 * data to use for training, and the remainder to use for validation. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <INSTANCE> Type of the instances in the dataset
 */
public class RandomisedPercentageHoldOut<INSTANCE> extends DefaultValidationData<ListDataset<INSTANCE>> {
	
	/**
	 * Construct with the given dataset and percentage of training
	 * data (0..1).
	 * 
	 * @param percentageTraining percentage of the dataset to use for training
	 * @param dataset the dataset
	 */
	public RandomisedPercentageHoldOut(double percentageTraining, ListDataset<INSTANCE> dataset) {
		if (percentageTraining < 0 || percentageTraining > 1)
			throw new IllegalArgumentException("percentage of training instances must be between 0 and 1");

		if (percentageTraining < 0.5) {
			int nTraining = (int)Math.round(percentageTraining * dataset.size());
			int [] trainKeys = RandomData.getUniqueRandomInts(nTraining, 0, dataset.size());
			
			training = new ListBackedDataset<INSTANCE>(new AcceptingListView<INSTANCE>(DatasetAdaptors.asList(dataset), trainKeys));
			validation = new ListBackedDataset<INSTANCE>(new SkippingListView<INSTANCE>(DatasetAdaptors.asList(dataset), trainKeys));
		} else {
			int nValidation = (int)Math.round((1.0 - percentageTraining) * dataset.size());
			int [] validationKeys = RandomData.getUniqueRandomInts(nValidation, 0, dataset.size());
			
			training = new ListBackedDataset<INSTANCE>(new SkippingListView<INSTANCE>(DatasetAdaptors.asList(dataset), validationKeys));
			validation = new ListBackedDataset<INSTANCE>(new AcceptingListView<INSTANCE>(DatasetAdaptors.asList(dataset), validationKeys));
		}
	}
}
