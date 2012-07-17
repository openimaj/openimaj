package org.openimaj.experiment.dataset.crossvalidation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.Identifiable;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapDataset;
import org.openimaj.util.list.AcceptingListView;
import org.openimaj.util.list.SkippingListView;

public class GroupedLeaveOneOutIterator<K, V extends Identifiable> implements Iterable<CrossValidationData<GroupedDataset<K, ListDataset<V>, V>>> {
	private GroupedDataset<K, ListDataset<V>, V> dataset;
	
	public GroupedLeaveOneOutIterator(MapDataset<K, ListDataset<V>, V> dataset) {
		this.dataset = dataset;
	}

	@Override
	public Iterator<CrossValidationData<GroupedDataset<K, ListDataset<V>, V>>> iterator() {
		return new Iterator<CrossValidationData<GroupedDataset<K, ListDataset<V>, V>>>() {
			int validationIndex = 0;
			int validationGroupIndex = 0;
			Iterator<K> groupIterator = dataset.getGroups().iterator();
			K currentGroup = groupIterator.hasNext() ? groupIterator.next() : null;
			ListDataset<V> currentValues = currentGroup == null ? null : dataset.getItems(currentGroup);
			
			@Override
			public boolean hasNext() {
				return validationIndex < dataset.size();
			}

			@Override
			public CrossValidationData<GroupedDataset<K, ListDataset<V>, V>> next() {
				int selectedIndex;
				
				if (currentValues != null && validationGroupIndex < currentValues.size()) {
					selectedIndex = validationGroupIndex;
					validationGroupIndex++;
				} else {
					validationGroupIndex = 0;
					currentGroup = groupIterator.next();
					currentValues = currentGroup == null ? null : dataset.getItems(currentGroup);
					
					return next();
				}
				
				Map<K, ListDataset<V>> train = new HashMap<K, ListDataset<V>>();
				for (K group : dataset.getGroups()) {
					if (group != currentGroup) 
						train.put(group, dataset.getItems(group));
				}
				train.put(currentGroup, new ListDataset<V>(new SkippingListView<V>(currentValues, selectedIndex)));
				
				Map<K, ListDataset<V>> valid = new HashMap<K, ListDataset<V>>();
				valid.put(currentGroup, new ListDataset<V>(new AcceptingListView<V>(currentValues, selectedIndex)));
				
				GroupedDataset<K, ListDataset<V>, V> cvTrain = new MapDataset<K, ListDataset<V>, V>(train);
				GroupedDataset<K, ListDataset<V>, V> cvValid = new MapDataset<K, ListDataset<V>, V>(valid);

				validationIndex++;
				
				return new CrossValidationData<GroupedDataset<K, ListDataset<V>, V>>(cvTrain, cvValid);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
