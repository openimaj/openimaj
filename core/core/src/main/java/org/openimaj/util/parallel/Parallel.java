/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.util.parallel;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ThreadPoolExecutor;

import org.openimaj.util.parallel.partition.GrowingChunkPartitioner;
import org.openimaj.util.parallel.partition.Partitioner;
import org.openimaj.util.parallel.partition.RangePartitioner;

/**
 * Parallel processing utilities for looping. 
 * 
 * Inspired by the .NET Task Parallel Library. Allows control over
 * the way data is partitioned using inspiration from 
 * {@link "http://reedcopsey.com/2010/01/26/parallelism-in-net-part-5-partitioning-of-work/"}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class Parallel {
	private static class Task<T> implements Runnable {
		private Iterator<T> iterator;
		private Operation<T> op;
		
		public Task(Iterator<T> iterator, Operation<T> op) {
			this.iterator = iterator;
			this.op = op;
		}
		
		@Override
		public void run() {
			while (iterator.hasNext()) {
				op.perform(iterator.next());
			}
		}		
	}
	
	/**
	 * Parallel integer for loop. 
	 * 
	 * @param start starting value
	 * @param stop stopping value
	 * @param incr increment amount
	 * @param op operation to perform
	 * @param pool the thread pool. 
	 */
	public static void For(final int start, final int stop, final int incr, final Operation<Integer> op, final ThreadPoolExecutor pool) {
		int loops = pool.getMaximumPoolSize();
		int ops = (stop - start) / incr;
		
		double div = ops / (double)loops;
		int chunksize = (int)div;
		int remainder = (int) ((div - chunksize) * loops);
		
		if (div < 1) {
			chunksize = 1;
			remainder = 0;
			loops = ops;
		}
		
        final CountDownLatch latch = new CountDownLatch(loops);
        
        for (int i=start; i<stop;) {
            final int lo = i;
            i += chunksize*incr;
            if (remainder > 0) {
            	i += incr;
            	remainder--;
            }
            
            final int hi = Math.min(i, stop);
            
            pool.submit(new Runnable() {
                @Override
				public void run() {
                    for (int i=lo; i<hi; i+= incr)
                        op.perform(i);
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {}		
	}
	
	/**
	 * Parallel integer for loop. Uses the default
	 * global thread pool.
	 * 
	 * @see GlobalExecutorPool#getPool()
	 * 
	 * @param start starting value
	 * @param stop stopping value
	 * @param incr increment amount
	 * @param op operation to perform 
	 */
	public static void For(final int start, final int stop, final int incr, final Operation<Integer> op) {
		For(start, stop, incr, op, GlobalExecutorPool.getPool());
	}
	
	/**
	 * Parallel ForEach loop over {@link Iterable} data. 
	 * The data is automatically partitioned; if the 
	 * data is a {@link List}, then a {@link RangePartitioner} is used,
	 * otherwise a {@link GrowingChunkPartitioner} is used.
	 * 
	 * @see GlobalExecutorPool#getPool()
	 * 
	 * @param <T> type of the data items
	 * @param objects the data
	 * @param op the operation to apply
	 * @param pool the thread pool.
	 */
	public static <T> void ForEach(final Iterable<T> objects, final Operation<T> op, final ThreadPoolExecutor pool) {
		Partitioner<T> partitioner;
		if (objects instanceof List) {
			partitioner = new RangePartitioner<T>((List<T>) objects);
		} else {
			partitioner = new GrowingChunkPartitioner<T>(objects);
		}
		ForEach(partitioner, op, pool);
	}
	
	/**
	 * Parallel ForEach loop over {@link Iterable} data. Uses the default
	 * global thread pool. The data is automatically partitioned; if the 
	 * data is a {@link List}, then a {@link RangePartitioner} is used,
	 * otherwise a {@link GrowingChunkPartitioner} is used.
	 * 
	 * @see GlobalExecutorPool#getPool()
	 * 
	 * @param <T> type of the data items
	 * @param objects the data
	 * @param op the operation to apply
	 */
	public static <T> void ForEach(final Iterable<T> objects, final Operation<T> op) {
		ForEach(objects, op, GlobalExecutorPool.getPool());
	}
	
	/**
	 * Parallel ForEach loop over partitioned data. Uses the default
	 * global thread pool.
	 * 
	 * @see GlobalExecutorPool#getPool()
	 * 
	 * @param <T> type of the data items
	 * @param partitioner the partitioner applied to the data
	 * @param op the operation to apply
	 */
	public static <T> void ForEach(final Partitioner<T> partitioner, final Operation<T> op) {
		ForEach(partitioner, op, GlobalExecutorPool.getPool());
	}
		
	/**
	 * Parallel ForEach loop over partitioned data.
	 * <p>
	 * Implementation details:
	 * 1.) create partitions enumerator
	 * 2.) schedule nprocs partitions
	 * 3.) while there are still partitions to process
	 * 	3.1) on completion of a partition schedule the next one
	 * 4.) wait for completion of remaining partitions
	 * 
	 * @param <T> type of the data items
	 * @param partitioner the partitioner applied to the data
	 * @param op the operation to apply
	 * @param pool the thread pool.
	 */
	public static <T> void ForEach(final Partitioner<T> partitioner, final Operation<T> op, final ThreadPoolExecutor pool) {
		ExecutorCompletionService<Boolean> completion = new ExecutorCompletionService<Boolean>(pool);
		Iterator<Iterator<T>> partitions = partitioner.getPartitions();
		long submitted = 0;
		

		for (int i=0; i<pool.getMaximumPoolSize(); i++) {
			if (!partitions.hasNext())
				break;
			
			completion.submit(new Task<T>(partitions.next(), op), true);
			submitted++;
		}
		
		while (partitions.hasNext()) {
			try {
				completion.take();
			} catch (InterruptedException e) {
			}
			completion.submit(new Task<T>(partitions.next(), op), true);
		}
		
		for (int i=0; i<submitted; i++) {
			try {
				completion.take();
			} catch (InterruptedException e) {
			}
		}
	}
}
