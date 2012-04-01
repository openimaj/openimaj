package org.openimaj.util.parallel.partition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.openimaj.util.parallel.Operation;

/**
 * A {@link RangePartitioner} partitions data of a known size into
 * a predefined number of equally sized partitions. If the time taken
 * for processing each partition with {@link Operation}s varies, 
 * then this partitioner is not load-balancing (so threads may end up 
 * waiting whilst others are still working).   
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T> Type of object in the partition
 */
public class RangePartitioner<T> implements Partitioner<T> {
	private final List<T> data;
	private final int numPartitions;
	private final int partitionSize;
	private int currentPartition = 0;
	private int remainder;
	
	/**
	 * Construct with a {@link List} of data and the given number
	 * of partitions.
	 * @param list the data
	 * @param numPartitions the number of partitions
	 */
	public RangePartitioner(List<T> list, int numPartitions) {
		this.data = list;
		
		int ops = data.size();
		double div = ops / (double)numPartitions;
		
		if (div >= 1) {
			partitionSize = (int) div;
			remainder = (int) ((div - partitionSize) * numPartitions);
			this.numPartitions = numPartitions;
		} else {
			partitionSize = 1;
			remainder = 0;
			this.numPartitions = ops;
		}
	}
	
	/**
	 * Construct with a {@link Collection} of data and the given number
	 * of partitions.
	 * @param c the data
	 * @param numPartitions the number of partitions
	 */
	public RangePartitioner(Collection<T> c, int numPartitions) {
		this(new ArrayList<T>(c), numPartitions);
	}
	
	/**
	 * Construct with an array of data and the given number
	 * of partitions.
	 * @param array the data
	 * @param numPartitions the number of partitions
	 */
	public RangePartitioner(T[] array, int numPartitions) {
		this(Arrays.asList(array), numPartitions);
	}
	
	/**
	 * Construct with a {@link List} of data and the number of
	 * partitions equal to the number of hardware threads.
	 * 
	 * @param list the data
	 */
	public RangePartitioner(List<T> list) {
		this(list, Runtime.getRuntime().availableProcessors());
	}
	
	/**
	 * Construct with a {@link Collection} of data and the number of
	 * partitions equal to the number of hardware threads.
	 * 
	 * @param c the data
	 */
	public RangePartitioner(Collection<T> c) {
		this(new ArrayList<T>(c), Runtime.getRuntime().availableProcessors());
	}
	
	/**
	 * Construct with an array of data and the number of
	 * partitions equal to the number of hardware threads.
	 * 
	 * @param array the data
	 */
	public RangePartitioner(T[] array) {
		this(Arrays.asList(array), Runtime.getRuntime().availableProcessors());
	}
	
	@Override
	public Iterator<Iterator<T>> getPartitions() {
		return new Iterator<Iterator<T>>() {
			int offset = 0;
			
			@Override
			public boolean hasNext() {
				return currentPartition < numPartitions;
			}

			@Override
			public Iterator<T> next() {
				int start = offset + currentPartition * partitionSize;
				int stop = Math.min(offset + (++currentPartition)*partitionSize, data.size());
				
				if (remainder > 0) {
					stop++;
					
					remainder--;
					offset++;
				}
				
				return data.subList(start, stop).iterator();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported");
			}
		};
	}

}
