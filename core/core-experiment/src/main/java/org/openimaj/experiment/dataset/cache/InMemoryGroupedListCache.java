package org.openimaj.experiment.dataset.cache;

import java.util.Collection;

import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapBackedDataset;

/**
 * In-memory implementation of a {@link GroupedListCache}
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *  
 * @param <OBJECT> Type of instances
 * @param <KEY> Type of groups
 */
public class InMemoryGroupedListCache<KEY, OBJECT> implements GroupedListCache<KEY, OBJECT> {
	MapBackedDataset<KEY, ListDataset<OBJECT>, OBJECT> dataset = new MapBackedDataset<KEY, ListDataset<OBJECT>, OBJECT>();

	@Override
	public void add(Collection<KEY> keys, OBJECT object) {
		for (KEY key : keys) {
			ListBackedDataset<OBJECT> list = (ListBackedDataset<OBJECT>) dataset.getInstances(key);
			if (list == null) dataset.getMap().put(key, list = new ListBackedDataset<OBJECT>());
			
			list.add(object);
		}
	}

	@Override
	public void add(KEY key, OBJECT object) {
		ListBackedDataset<OBJECT> list = (ListBackedDataset<OBJECT>) dataset.getInstances(key);
		if (list == null) dataset.getMap().put(key, list = new ListBackedDataset<OBJECT>());
		
		list.add(object);
	}

	@Override
	public void add(KEY key, Collection<OBJECT> objects) {
		ListBackedDataset<OBJECT> list = (ListBackedDataset<OBJECT>) dataset.getInstances(key);
		if (list == null) dataset.getMap().put(key, list = new ListBackedDataset<OBJECT>());
		
		for (OBJECT object : objects)
			list.add(object);
	}

	@Override
	public GroupedDataset<KEY, ListDataset<OBJECT>, OBJECT> getDataset() {
		return dataset;
	}

	@Override
	public void reset() {
		dataset.getMap().clear();
	}
}
