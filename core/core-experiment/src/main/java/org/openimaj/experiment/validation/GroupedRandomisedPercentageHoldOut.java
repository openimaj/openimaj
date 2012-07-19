package org.openimaj.experiment.validation;

import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListDataset;

public class GroupedRandomisedPercentageHoldOut<KEY, INSTANCE> extends DefaultValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>> {

	public GroupedRandomisedPercentageHoldOut(double percentageTraining, GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> dataset) {
		
		
	}

}
