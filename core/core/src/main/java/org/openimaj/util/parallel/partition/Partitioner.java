package org.openimaj.util.parallel.partition;

import java.util.Iterator;

/**
 * A {@link Partitioner} partitions data into subsets that can
 * be processed in parallel. {@link Partitioner}s are used to 
 * reduce the overhead in distributing work across multiple
 * processors.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T>
 */
public interface Partitioner<T> {
	/**
	 * @return the partitioned data.
	 */
	public Iterator<Iterator<T>> getPartitions();
}
