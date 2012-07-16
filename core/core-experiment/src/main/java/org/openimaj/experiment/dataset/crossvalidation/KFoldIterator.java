package org.openimaj.experiment.dataset.crossvalidation;

import java.util.Arrays;
import java.util.Iterator;

import org.openimaj.data.RandomData;
import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.experiment.dataset.Identifiable;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.util.list.AcceptingListView;
import org.openimaj.util.list.SkippingListView;

public class KFoldIterator<T extends Identifiable> implements Iterable<CrossValidationData<Dataset<T>>> {
	private Dataset<T> dataset;
	private int[][] subsetIndices;
	
	public KFoldIterator(Dataset<T> dataset, int k) {
		if (k > dataset.size())
			throw new IllegalArgumentException("The number of folds must be less than the number of items in the dataset");
		
		this.dataset = dataset;
		
		int[] allIndices = RandomData.getUniqueRandomInts(dataset.size(), 0, dataset.size());
		subsetIndices = new int[k][];
		
		int splitSize = dataset.size() / k;
		for (int i=0; i<k-1; i++) { 
			subsetIndices[k] = Arrays.copyOfRange(allIndices, splitSize * i, splitSize * (i + 1));
		}
		subsetIndices[k-1] = Arrays.copyOfRange(allIndices, splitSize * k-1, allIndices.length);
	}
	
	@Override
	public Iterator<CrossValidationData<Dataset<T>>> iterator() {
		return new Iterator<CrossValidationData<Dataset<T>>>() {
			int validationSubset = 0;
			
			@Override
			public boolean hasNext() {
				return validationSubset < subsetIndices.length;
			}

			@Override
			public CrossValidationData<Dataset<T>> next() {
				ListDataset<T> training = new ListDataset<T>(new SkippingListView<T>(dataset, subsetIndices[validationSubset]));
				ListDataset<T> validation = new ListDataset<T>(new AcceptingListView<T>(dataset, subsetIndices[validationSubset]));
				
				validationSubset++;
				
				return new CrossValidationData<Dataset<T>>(training, validation);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
