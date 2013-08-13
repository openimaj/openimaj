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
package org.openimaj.ml.clustering.kmeans;

import java.util.concurrent.ExecutorService;

import org.openimaj.knn.NearestNeighbours;
import org.openimaj.knn.NearestNeighboursFactory;
import org.openimaj.util.parallel.GlobalExecutorPool;

/**
 * Configuration for the K-Means algorithm.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <NN>
 *            The type of {@link NearestNeighbours} to use
 * @param <DATA>
 *            The type of data
 */
public class KMeansConfiguration<NN extends NearestNeighbours<DATA, ?, ?>, DATA> implements Cloneable {
	/**
	 * The default number of samples per parallel assignment instance.
	 */
	public static final int DEFAULT_BLOCK_SIZE = 50000;

	/**
	 * The default number of iterations.
	 */
	public static final int DEFAULT_NUMBER_ITERATIONS = 30;

	/**
	 * The number of clusters
	 */
	protected int K;

	/**
	 * The factory for producing the {@link NearestNeighbours} objects used in
	 * assignment.
	 */
	protected NearestNeighboursFactory<? extends NN, DATA> factory;

	/**
	 * The size of processing blocks for each thread
	 */
	protected int blockSize;

	/**
	 * The max number of iterations
	 */
	protected int niters;

	/**
	 * The threadpool for parallel processing
	 */
	protected ExecutorService threadpool;

	/**
	 * Create configuration for data that will create <code>K</code> clusters.
	 * The algorithm will run for a maximum of
	 * {@link #DEFAULT_NUMBER_ITERATIONS} iterations, and make use of all
	 * available processors, processing with blocks of
	 * {@link #DEFAULT_BLOCK_SIZE} vectors.
	 * <p>
	 * The specified {@link NearestNeighboursFactory} determines the actual type
	 * of k-means that will be performed; it could be exact nearest-neighbours,
	 * or it could be an approximate method, for example based on an ensemble of
	 * kd-trees.
	 * 
	 * @param K
	 *            number of clusters to be found
	 * @param nnFactory
	 *            the factory for producing the {@link NearestNeighbours}.
	 */
	public KMeansConfiguration(int K, NearestNeighboursFactory<? extends NN, DATA> nnFactory) {
		this(K, nnFactory, DEFAULT_NUMBER_ITERATIONS, DEFAULT_BLOCK_SIZE, GlobalExecutorPool.getPool());
	}

	/**
	 * Create configuration for data that will create <code>K</code> clusters.
	 * The algorithm will run for a maximum of the given number of iterations,
	 * and will make use of all available processors, processing with blocks of
	 * {@link #DEFAULT_BLOCK_SIZE} vectors.
	 * <p>
	 * The specified {@link NearestNeighboursFactory} determines the actual type
	 * of k-means that will be performed; it could be exact nearest-neighbours,
	 * or it could be an approximate method, for example based on an ensemble of
	 * kd-trees.
	 * 
	 * @param K
	 *            number of clusters to be found
	 * @param nnFactory
	 *            the factory for producing the {@link NearestNeighbours}.
	 * @param niters
	 *            number of iterations
	 */
	public KMeansConfiguration(int K, NearestNeighboursFactory<? extends NN, DATA> nnFactory, int niters) {
		this(K, nnFactory, niters, DEFAULT_BLOCK_SIZE, GlobalExecutorPool.getPool());
	}

	/**
	 * Create configuration for data that will create <code>K</code> clusters.
	 * The algorithm will run for a maximum of the given number of iterations,
	 * and will make use of the provided threadpool, processing with blocks of
	 * {@link #DEFAULT_BLOCK_SIZE} vectors.
	 * <p>
	 * The specified {@link NearestNeighboursFactory} determines the actual type
	 * of k-means that will be performed; it could be exact nearest-neighbours,
	 * or it could be an approximate method, for example based on an ensemble of
	 * kd-trees.
	 * 
	 * @param K
	 *            number of clusters to be found
	 * @param nnFactory
	 *            the factory for producing the {@link NearestNeighbours}.
	 * @param threadpool
	 *            threadpool to use for parallel processing
	 * @param niters
	 *            number of training iterations
	 */
	public KMeansConfiguration(int K, NearestNeighboursFactory<? extends NN, DATA> nnFactory, int niters,
			ExecutorService threadpool)
	{
		this(K, nnFactory, niters, DEFAULT_BLOCK_SIZE, threadpool);
	}

	/**
	 * Create configuration for data with <code>M</code> dimensions that will
	 * create <code>K</code> clusters. The algorithm will run for a maximum of
	 * the given number of iterations, and will make use of
	 * <code>nThreads</code> processors, processing with blocks of
	 * {@link #DEFAULT_BLOCK_SIZE} vectors.
	 * <p>
	 * The specified {@link NearestNeighboursFactory} determines the actual type
	 * of k-means that will be performed; it could be exact nearest-neighbours,
	 * or it could be an approximate method, for example based on an ensemble of
	 * kd-trees.
	 * 
	 * @param K
	 *            number of clusters to be found
	 * @param nnFactory
	 *            the factory for producing the {@link NearestNeighbours}.
	 * @param threadpool
	 *            threadpool to use for parallel processing
	 * @param blockSize
	 *            number of samples per parallel thread
	 * @param niters
	 *            number of training iterations
	 */
	public KMeansConfiguration(int K, NearestNeighboursFactory<? extends NN, DATA> nnFactory, int niters,
			int blockSize, ExecutorService threadpool)
	{
		this.K = K;
		this.factory = nnFactory;
		this.niters = niters;
		this.blockSize = blockSize;
		this.threadpool = (threadpool == null ? GlobalExecutorPool.getPool() : threadpool);
	}

	/**
	 * A completely default configuration used primarily as a convenience
	 * function for reading. The number of dimensions, number of clusters and
	 * nearest-neighbours factory must be set before the configuration is used.
	 */
	public KMeansConfiguration() {
		this(0, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public KMeansConfiguration<NN, DATA> clone() {
		try {
			return (KMeansConfiguration<NN, DATA>) super.clone();
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get the number of clusters
	 * 
	 * @return the number of clusters
	 */
	public int getK() {
		return K;
	}

	/**
	 * Set the number of clusters
	 * 
	 * @param k
	 *            the number of clusters
	 */
	public void setK(int k) {
		K = k;
	}

	/**
	 * Get the number of clusters
	 * 
	 * @return the number of clusters
	 */
	public int numClusters() {
		return K;
	}

	/**
	 * Set the number of clusters
	 * 
	 * @param k
	 *            the number of clusters
	 */
	public void setNumClusters(int k) {
		K = k;
	}

	/**
	 * Get the number of samples processed in a batch by a thread. This needs to
	 * be small enough that that the memory isn't exhausted, but big enough for
	 * the thread to have enough data to work for a while.
	 * 
	 * @return the the number of samples processed in a batch by a thread
	 */
	public int getBlockSize() {
		return blockSize;
	}

	/**
	 * Set the number of samples processed in a batch by a thread. This needs to
	 * be small enough that that the memory isn't exhausted, but big enough for
	 * the thread to have enough data to work for a while.
	 * 
	 * @param blockSize
	 *            the number of samples processed in a batch by a thread
	 */
	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	/**
	 * Get the maximum allowed number of iterations.
	 * 
	 * @return the maximum allowed number of iterations.
	 */
	public int getMaxIterations() {
		return niters;
	}

	/**
	 * Set the maximum allowed number of iterations.
	 * 
	 * @param niters
	 *            the maximum allowed number of iterations.
	 */
	public void setMaxIterations(int niters) {
		this.niters = niters;
	}

	/**
	 * Get the factory that produces the {@link NearestNeighbours} during
	 * clustering.
	 * 
	 * @return the factory
	 */
	public NearestNeighboursFactory<? extends NN, DATA> getNearestNeighbourFactory() {
		return factory;
	}

	/**
	 * Set the factory that produces the {@link NearestNeighbours} during
	 * clustering.
	 * 
	 * @param factory
	 *            the factory to set
	 */
	public void setNearestNeighbourFactory(NearestNeighboursFactory<? extends NN, DATA> factory) {
		this.factory = factory;
	}
}
