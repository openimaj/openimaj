package org.openimaj.util.parallel.partition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A {@link FixedSizeChunkPartitioner} dynamically partitions data into chunks
 * of a fixed length. The partitioner does not need to know
 * about the length of the data apriori. 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T> Type of object in the partition
 */
public class FixedSizeChunkPartitioner<T> implements Partitioner<T> {
	private Iterator<T> objects;
	private int chunkSize = 20;
	
	/**
	 * Construct with data in the form of an {@link Iterable}. The number of
	 * items per chunk is set at the default value (20).
	 * 
	 * @param objects the {@link Iterable} representing the data.
	 */
	public FixedSizeChunkPartitioner(Iterable<T> objects) {
		this.objects = objects.iterator();
	}
	
	/**
	 * Construct with data in the form of an {@link Iterable} and the given
	 * number of items per chunk.
	 * 
	 * @param objects the {@link Iterable} representing the data.
	 * @param chunkSize number of items in each chunk.
	 */
	public FixedSizeChunkPartitioner(Iterable<T> objects, int chunkSize) {
		this.objects = objects.iterator();
	}

	@Override
	public Iterator<Iterator<T>> getPartitions() {
		return new Iterator<Iterator<T>>() {

			@Override
			public boolean hasNext() {
				synchronized (objects) {
					return objects.hasNext();
				}
			}

			@Override
			public Iterator<T> next() {
				synchronized (objects) {
					if (!objects.hasNext())
						return null;
					
					List<T> list = new ArrayList<T>(chunkSize);
					
					int i=0;
					while (objects.hasNext() && i<chunkSize) {
						list.add(objects.next());
						i++;
					}
					
					return list.iterator();
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported");
			}
		};
	}
}
