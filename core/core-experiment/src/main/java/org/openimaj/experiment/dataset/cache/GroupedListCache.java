package org.openimaj.experiment.dataset.cache;

import java.util.Collection;

import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListDataset;

/**
 * Definition of a cache for the groups of lists.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OBJECT> Type of instances
 * @param <KEY> Type of groups
 */
public interface GroupedListCache<KEY, OBJECT> {
	/**
	 * Add an object with many keys to the cache
	 * @param keys the instance's keys
	 * @param object the instance
	 */
	public void add(Collection<KEY> keys, OBJECT object);
	
	/**
	 * Add an object with a key to the cache
	 * @param key the instance's key
	 * @param object the instance
	 */
	public void add(KEY key, OBJECT object);
	
	/**
	 * Add an collection of objects with the same key to the cache
	 * @param key the instance's key
	 * @param objects the instances
	 */
	public void add(KEY key, Collection<OBJECT> objects);
	
	/**
	 * @return a dataset view of the cache
	 */
	public GroupedDataset<KEY, ListDataset<OBJECT>, OBJECT> getDataset();
	
	/**
	 * Reset the cache 
	 */
	public void reset();
}