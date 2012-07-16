package org.openimaj.experiment.dataset.crossvalidation;

import java.util.Iterator;

import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.experiment.dataset.Identifiable;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.util.list.AcceptingListView;
import org.openimaj.util.list.SkippingListView;

public class LeaveOneOutIterator<T extends Identifiable> implements Iterable<CrossValidationData<Dataset<T>>> {
	private Dataset<T> dataset;
	
	@Override
	public Iterator<CrossValidationData<Dataset<T>>> iterator() {
		return new Iterator<CrossValidationData<Dataset<T>>>() {
			int validationIndex = 0;
			
			@Override
			public boolean hasNext() {
				return validationIndex < dataset.size();
			}

			@Override
			public CrossValidationData<Dataset<T>> next() {
				ListDataset<T> training = new ListDataset<T>(new SkippingListView<T>(dataset, validationIndex));
				ListDataset<T> validation = new ListDataset<T>(new AcceptingListView<T>(dataset, validationIndex));
				
				validationIndex++;
				
				return new CrossValidationData<Dataset<T>>(training, validation);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
