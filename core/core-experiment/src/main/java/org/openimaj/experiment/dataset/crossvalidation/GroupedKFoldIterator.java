package org.openimaj.experiment.dataset.crossvalidation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openimaj.data.RandomData;
import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.Identifiable;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapDataset;
import org.openimaj.util.list.AcceptingListView;
import org.openimaj.util.list.SkippingListView;

public class GroupedKFoldIterator<K, V extends Identifiable> implements Iterable<CrossValidationData<GroupedDataset<K, ListDataset<V>, V>>> {
	private GroupedDataset<K, ListDataset<V>, V> dataset;
	private Map<K, int[][]> subsetIndices = new HashMap<K, int[][]>();
	private int numFolds;
	
	public GroupedKFoldIterator(GroupedDataset<K, ListDataset<V>, V> dataset, int k) {
		if (k > dataset.size())
			throw new IllegalArgumentException("The number of folds must be less than the number of items in the dataset");
		
		this.dataset = dataset;
		this.numFolds = k;
		
		Set<K> keys = dataset.getGroups();
		for (K group : keys) {
			if (dataset.getItems(group).size() < numFolds)
				numFolds = dataset.getGroups().size();
		}
		
		for (K group : keys) {
			int keySize = dataset.getItems(group).size();
			
			int[] allKeyIndices = RandomData.getUniqueRandomInts(keySize, 0, keySize);
			
			subsetIndices.put(group, new int[numFolds][]);
			int[][] si = subsetIndices.get(group);
		
			int splitSize = keySize / numFolds;
			for (int i=0; i<numFolds-1; i++) { 
				si[numFolds] = Arrays.copyOfRange(allKeyIndices, splitSize * i, splitSize * (i + 1));
			}
			si[numFolds-1] = Arrays.copyOfRange(allKeyIndices, splitSize * numFolds-1, allKeyIndices.length);
		}
	}
	
	@Override
	public Iterator<CrossValidationData<GroupedDataset<K, ListDataset<V>, V>>> iterator() {
		return new Iterator<CrossValidationData<GroupedDataset<K, ListDataset<V>, V>>>() {
			int validationSubset = 0;
			
			@Override
			public boolean hasNext() {
				return validationSubset < numFolds;
			}

			@Override
			public CrossValidationData<GroupedDataset<K, ListDataset<V>, V>> next() {
				Map<K, ListDataset<V>> train = new HashMap<K, ListDataset<V>>();
				Map<K, ListDataset<V>> valid = new HashMap<K, ListDataset<V>>();
				
				for (K group : subsetIndices.keySet()) {
					int[][] si = subsetIndices.get(group);
					
					train.put(group, new ListDataset<V>(new SkippingListView<V>(dataset, si[validationSubset])));
					valid.put(group, new ListDataset<V>(new AcceptingListView<V>(dataset, si[validationSubset])));
				}
				
				MapDataset<K, ListDataset<V>, V> cvTrain = new MapDataset<K, ListDataset<V>, V>(train);
				MapDataset<K, ListDataset<V>, V> cvValid = new MapDataset<K, ListDataset<V>, V>(valid);
				
				validationSubset++;
				
				return new CrossValidationData<GroupedDataset<K, ListDataset<V>, V>>(cvTrain, cvValid);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
