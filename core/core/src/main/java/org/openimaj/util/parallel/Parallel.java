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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ThreadPoolExecutor;

import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.partition.FixedSizeChunkPartitioner;
import org.openimaj.util.parallel.partition.GrowingChunkPartitioner;
import org.openimaj.util.parallel.partition.Partitioner;
import org.openimaj.util.parallel.partition.RangePartitioner;
import org.openimaj.util.stream.Stream;

/**
 * Parallel processing utilities for looping.
 * <p>
 * Inspired by the .NET Task Parallel Library. Allows control over the way data
 * is partitioned using inspiration from <a href=
 * "http://reedcopsey.com/2010/01/26/parallelism-in-net-part-5-partitioning-of-work/"
 * >Reed Copsey's blog</a>.
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

	private static class BatchTask<T> implements Runnable {
		private Iterator<T> iterator;
		private Operation<Iterator<T>> op;

		public BatchTask(Iterator<T> iterator, Operation<Iterator<T>> op) {
			this.iterator = iterator;
			this.op = op;
		}

		@Override
		public void run() {
			op.perform(iterator);
		}
	}

	/**
	 * An integer range with a step size.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static class IntRange {
		/**
		 * Starting value (inclusive)
		 */
		public final int start;

		/**
		 * Stopping value (exclusive)
		 */
		public final int stop;

		/**
		 * Increment amount
		 */
		public final int incr;

		IntRange(int start, int stop, int incr) {
			this.start = start;
			this.stop = stop;
			this.incr = incr;
		}
	}

	/**
	 * Parallel integer for loop.
	 *
	 * @param start
	 *            starting value
	 * @param stop
	 *            stopping value
	 * @param incr
	 *            increment amount
	 * @param op
	 *            operation to perform
	 * @param pool
	 *            the thread pool.
	 */
	public static void forIndex(final int start, final int stop, final int incr, final Operation<Integer> op,
			final ThreadPoolExecutor pool)
	{
		int loops = pool.getMaximumPoolSize();
		final int ops = (stop - start) / incr;

		final double div = ops / (double) loops;
		int chunksize = (int) div;
		int remainder = (int) ((div - chunksize) * loops);

		if (div < 1) {
			chunksize = 1;
			remainder = 0;
			loops = ops;
		}

		final CountDownLatch latch = new CountDownLatch(loops);

		for (int i = start; i < stop;) {
			final int lo = i;
			i += chunksize * incr;
			if (remainder > 0) {
				i += incr;
				remainder--;
			}

			final int hi = Math.min(i, stop);

			pool.submit(new Runnable() {
				@Override
				public void run() {
					for (int i = lo; i < hi; i += incr)
						op.perform(i);
					latch.countDown();
				}
			});
		}
		try {
			latch.await();
		} catch (final InterruptedException e) {
		}
	}

	/**
	 * Parallel integer for loop. Uses the default global thread pool.
	 *
	 * @see GlobalExecutorPool#getPool()
	 *
	 * @param start
	 *            starting value
	 * @param stop
	 *            stopping value
	 * @param incr
	 *            increment amount
	 * @param op
	 *            operation to perform
	 */
	public static void forIndex(final int start, final int stop, final int incr, final Operation<Integer> op) {
		forIndex(start, stop, incr, op, GlobalExecutorPool.getPool());
	}

	/**
	 * Parallel integer for loop. Fundamentally this is the same as
	 * {@link #forIndex(int, int, int, Operation)}, but potentially slightly
	 * faster as it avoids auto-boxing/unboxing and results in fewer method
	 * calls. The downside is that users have to write an extra loop to iterate
	 * over the {@link IntRange} object. Uses the default global thread pool.
	 *
	 * @param start
	 *            starting value
	 * @param stop
	 *            stopping value
	 * @param incr
	 *            increment amount
	 * @param op
	 *            operation to perform
	 */
	public static void forRange(final int start, final int stop, final int incr, final Operation<IntRange> op) {
		forRange(start, stop, incr, op, GlobalExecutorPool.getPool());
	}

	/**
	 * Parallel integer for loop. Fundamentally this is the same as
	 * {@link #forIndex(int, int, int, Operation, ThreadPoolExecutor)}, but
	 * potentially slightly faster as it avoids auto-boxing/unboxing and results
	 * in fewer method calls. The downside is that users have to write an extra
	 * loop to iterate over the {@link IntRange} object.
	 *
	 * @param start
	 *            starting value
	 * @param stop
	 *            stopping value
	 * @param incr
	 *            increment amount
	 * @param op
	 *            operation to perform
	 * @param pool
	 *            the thread pool.
	 */
	public static void forRange(final int start, final int stop, final int incr, final Operation<IntRange> op,
			final ThreadPoolExecutor pool)
	{
		int loops = pool.getMaximumPoolSize();
		final int ops = (stop - start) / incr;

		final double div = ops / (double) loops;
		int chunksize = (int) div;
		int remainder = (int) ((div - chunksize) * loops);

		if (div < 1) {
			chunksize = 1;
			remainder = 0;
			loops = ops;
		}

		final CountDownLatch latch = new CountDownLatch(loops);
		final Thread thread = Thread.currentThread();
		final Throwable[] exception = new Throwable[1];

		for (int i = start; i < stop;) {
			final int lo = i;
			i += chunksize * incr;
			if (remainder > 0) {
				i += incr;
				remainder--;
			}

			final int hi = Math.min(i, stop);

			pool.submit(new Runnable() {
				@Override
				public void run() {
					try {
						op.perform(new IntRange(lo, hi, incr));
					} catch (final Throwable t) {
						exception[0] = t;
						thread.interrupt();
					} finally {
						latch.countDown();
					}
				}
			});
		}
		try {
			latch.await();
		} catch (final InterruptedException e) {
			if (exception[0] instanceof Error)
				throw (Error) exception[0];
			if (exception[0] instanceof RuntimeException)
				throw (RuntimeException) exception[0];
			throw new RuntimeException(exception[0]);
		}
	}

	/**
	 * Parallel ForEach loop over {@link Iterable} data. The data is
	 * automatically partitioned; if the data is a {@link List}, then a
	 * {@link RangePartitioner} is used, otherwise a
	 * {@link GrowingChunkPartitioner} is used.
	 *
	 * @see GlobalExecutorPool#getPool()
	 *
	 * @param <T>
	 *            type of the data items
	 * @param objects
	 *            the data
	 * @param op
	 *            the operation to apply
	 * @param pool
	 *            the thread pool.
	 */
	public static <T> void forEach(final Iterable<T> objects, final Operation<T> op, final ThreadPoolExecutor pool) {
		Partitioner<T> partitioner;
		if (objects instanceof List) {
			partitioner = new RangePartitioner<T>((List<T>) objects, pool.getMaximumPoolSize());
		} else {
			partitioner = new GrowingChunkPartitioner<T>(objects);
		}
		forEach(partitioner, op, pool);
	}

	/**
	 * Parallel ForEach loop over {@link Iterable} data. Uses the default global
	 * thread pool. The data is automatically partitioned; if the data is a
	 * {@link List}, then a {@link RangePartitioner} is used, otherwise a
	 * {@link GrowingChunkPartitioner} is used.
	 *
	 * @see GlobalExecutorPool#getPool()
	 *
	 * @param <T>
	 *            type of the data items
	 * @param objects
	 *            the data
	 * @param op
	 *            the operation to apply
	 */
	public static <T> void forEach(final Iterable<T> objects, final Operation<T> op) {
		forEach(objects, op, GlobalExecutorPool.getPool());
	}

	/**
	 * Parallel ForEach loop over partitioned data. Uses the default global
	 * thread pool.
	 *
	 * @see GlobalExecutorPool#getPool()
	 *
	 * @param <T>
	 *            type of the data items
	 * @param partitioner
	 *            the partitioner applied to the data
	 * @param op
	 *            the operation to apply
	 */
	public static <T> void forEach(final Partitioner<T> partitioner, final Operation<T> op) {
		forEach(partitioner, op, GlobalExecutorPool.getPool());
	}

	/**
	 * Parallel ForEach loop over partitioned data.
	 * <p>
	 * Implementation details: 1.) create partitions enumerator 2.) schedule
	 * nprocs partitions 3.) while there are still partitions to process 3.1) on
	 * completion of a partition schedule the next one 4.) wait for completion
	 * of remaining partitions
	 *
	 * @param <T>
	 *            type of the data items
	 * @param partitioner
	 *            the partitioner applied to the data
	 * @param op
	 *            the operation to apply
	 * @param pool
	 *            the thread pool.
	 */
	public static <T>
	void
	forEach(final Partitioner<T> partitioner, final Operation<T> op, final ThreadPoolExecutor pool)
	{
		final ExecutorCompletionService<Boolean> completion = new ExecutorCompletionService<Boolean>(pool);
		final Iterator<Iterator<T>> partitions = partitioner.getPartitions();
		long submitted = 0;

		for (int i = 0; i < pool.getMaximumPoolSize(); i++) {
			if (!partitions.hasNext())
				break;

			completion.submit(new Task<T>(partitions.next(), op), true);
			submitted++;
		}

		while (partitions.hasNext()) {
			try {
				completion.take().get();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			} catch (final ExecutionException e) {
				e.printStackTrace();
			}
			completion.submit(new Task<T>(partitions.next(), op), true);
		}

		for (int i = 0; i < submitted; i++) {
			try {
				completion.take().get();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			} catch (final ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Parallel ForEach loop over unpartitioned data. This is effectively the
	 * same as using a {@link FixedSizeChunkPartitioner} with a chunk size of 1,
	 * but with slightly less overhead. The unpartitioned for-each loop has
	 * slightly less throughput than a partitioned for-each loop, but exhibits
	 * much less delay in scheduling an item for processing as a partition does
	 * not have to first be populated. The unpartitioned for-each loop is
	 * particularly useful for processing temporal {@link Stream}s of data.
	 * <p>
	 * Implementation details: 1.) create partitions enumerator 2.) schedule
	 * nprocs partitions 3.) while there are still partitions to process 3.1) on
	 * completion of a partition schedule the next one 4.) wait for completion
	 * of remaining partitions
	 *
	 * @param <T>
	 *            type of the data items
	 * @param data
	 *            the iterator of data items
	 * @param op
	 *            the operation to apply
	 */
	public static <T>
	void
	forEachUnpartitioned(final Iterator<T> data, final Operation<T> op)
	{
		forEachUnpartitioned(data, op, GlobalExecutorPool.getPool());
	}

	/**
	 * Parallel ForEach loop over unpartitioned data. This is effectively the
	 * same as using a {@link FixedSizeChunkPartitioner} with a chunk size of 1,
	 * but with slightly less overhead. The unpartitioned for-each loop has
	 * slightly less throughput than a partitioned for-each loop, but exhibits
	 * much less delay in scheduling an item for processing as a partition does
	 * not have to first be populated. The unpartitioned for-each loop is
	 * particularly useful for processing temporal {@link Stream}s of data.
	 * <p>
	 * Implementation details: 1.) create partitions enumerator 2.) schedule
	 * nprocs partitions 3.) while there are still partitions to process 3.1) on
	 * completion of a partition schedule the next one 4.) wait for completion
	 * of remaining partitions
	 *
	 * @param <T>
	 *            type of the data items
	 * @param data
	 *            the iterator of data items
	 * @param op
	 *            the operation to apply
	 * @param pool
	 *            the thread pool.
	 */
	public static <T>
	void
	forEachUnpartitioned(final Iterator<T> data, final Operation<T> op, final ThreadPoolExecutor pool)
	{
		final ExecutorCompletionService<Boolean> completion = new ExecutorCompletionService<Boolean>(pool);
		long submitted = 0;

		for (int i = 0; i < pool.getMaximumPoolSize(); i++) {
			if (!data.hasNext())
				break;

			final T next = data.next();

			completion.submit(new Runnable() {
				@Override
				public void run() {
					op.perform(next);
				}
			}, true);
			submitted++;
		}

		while (data.hasNext()) {
			final T next = data.next();

			try {
				completion.take().get();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			} catch (final ExecutionException e) {
				e.printStackTrace();
			}
			completion.submit(new Runnable() {
				@Override
				public void run() {
					op.perform(next);
				}
			}, true);
		}

		for (int i = 0; i < submitted; i++) {
			try {
				completion.take().get();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			} catch (final ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Parallel ForEach loop over partitioned data with batches of data.
	 * <p>
	 * Implementation details: 1.) create partitions enumerator 2.) schedule
	 * nprocs partitions 3.) while there are still partitions to process 3.1) on
	 * completion of a partition schedule the next one 4.) wait for completion
	 * of remaining partitions
	 *
	 * @param <T>
	 *            type of the data items
	 * @param partitioner
	 *            the partitioner applied to the data
	 * @param op
	 *            the operation to apply
	 * @param pool
	 *            the thread pool.
	 */
	public static <T>
	void
	forEachPartitioned(final Partitioner<T> partitioner, final Operation<Iterator<T>> op,
			final ThreadPoolExecutor pool)
	{
		final ExecutorCompletionService<Boolean> completion = new ExecutorCompletionService<Boolean>(pool);
		final Iterator<Iterator<T>> partitions = partitioner.getPartitions();
		long submitted = 0;

		for (int i = 0; i < pool.getMaximumPoolSize(); i++) {
			if (!partitions.hasNext())
				break;

			completion.submit(new BatchTask<T>(partitions.next(), op), true);
			submitted++;
		}

		while (partitions.hasNext()) {
			try {
				completion.take().get();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			} catch (final ExecutionException e) {
				e.printStackTrace();
			}
			completion.submit(new BatchTask<T>(partitions.next(), op), true);
		}

		for (int i = 0; i < submitted; i++) {
			try {
				completion.take().get();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			} catch (final ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Parallel ForEach loop over batched partitioned data. Uses the default
	 * global thread pool.
	 *
	 * @see GlobalExecutorPool#getPool()
	 *
	 * @param <T>
	 *            type of the data items
	 * @param partitioner
	 *            the partitioner applied to the data
	 * @param op
	 *            the operation to apply
	 */
	public static <T> void forEachPartitioned(final Partitioner<T> partitioner, final Operation<Iterator<T>> op) {
		forEachPartitioned(partitioner, op, GlobalExecutorPool.getPool());
	}
}
