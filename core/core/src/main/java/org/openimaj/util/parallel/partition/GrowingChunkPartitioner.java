package org.openimaj.util.parallel.partition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A {@link GrowingChunkPartitioner} dynamically partitions data into chunks. 
 * The partitioner does not need to know about the length of the data apriori.
 * The size of the chunks grows over time exponentially.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T> Type of object in the partition
 */
public class GrowingChunkPartitioner<T> implements Partitioner<T> {
	private static final int NUM_HW_THREADS = Runtime.getRuntime().availableProcessors();
	private Iterator<T> objects;
	
	/**
	 * Construct with data in the form of an {@link Iterable}. 
	 * 
	 * @param objects the {@link Iterable} representing the data.
	 */
	public GrowingChunkPartitioner(Iterable<T> objects) {
		this.objects = objects.iterator();
	}

	@Override
	public Iterator<Iterator<T>> getPartitions() {
		return new Iterator<Iterator<T>>() {
			private int currentIteration = 0;
			private int chunkSize = 2;
			
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
					
					if (currentIteration % NUM_HW_THREADS == 0) {
						chunkSize = (int)Math.pow(2, (currentIteration/NUM_HW_THREADS) + 1);
					}
					
					List<T> list = new ArrayList<T>(chunkSize);
					
					int i=0;
					while (objects.hasNext() && i<chunkSize) {
						list.add(objects.next());
						i++;
					}
					
					currentIteration++;
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
