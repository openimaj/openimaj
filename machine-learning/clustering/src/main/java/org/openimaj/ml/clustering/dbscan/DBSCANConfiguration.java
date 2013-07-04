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
package org.openimaj.ml.clustering.dbscan;

import org.openimaj.knn.NearestNeighbours;
import org.openimaj.knn.NearestNeighboursFactory;

/**
 * Configuration for the DBSCAN algorithm.
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <NN>
 *            The type of {@link NearestNeighbours} to use
 * @param <DATA>
 *            The type of data
 */
public class DBSCANConfiguration<NN extends NearestNeighbours<DATA, ?, ?>, DATA> implements Cloneable {

	/**
	 * The number of dimensions
	 */
	protected int M;


	/**
	 * The factory for producing the {@link NearestNeighbours} objects used in
	 * assignment.
	 */
	protected NearestNeighboursFactory<? extends NN, DATA> factory;


	/***
	 * The threshold on the distance function which defines neighbours
	 */
	protected double eps;

	/**
	 * Minimum number of points such that a group of neighbours are considered a cluster (rather than noise)
	 */
	protected int minPts;


	/**
	 * Create configuration for data with <code>M</code> dimensions .
	 * <p>
	 * The specified {@link NearestNeighboursFactory} determines the actual type
	 * of DBSCAN that will be performed; it could be exact nearest-neighbours,
	 * or it could be an approximate method, for example based on an ensemble of
	 * kd-trees.
	 *
	 * @param M
	 *            number of elements in the data points.
	 * @param nnFactory
	 *            the factory for producing the {@link NearestNeighbours}.
	 */
	public DBSCANConfiguration(int M, double eps, int minPts, NearestNeighboursFactory<? extends NN, DATA> nnFactory)
	{
		this.M = M;
		this.eps = eps;
		this.minPts = minPts;
		this.factory = nnFactory;
	}

	/**
	 * A completely default configuration used primarily as a convenience
	 * function for reading. The number of dimensions, number of clusters and
	 * nearest-neighbours factory must be set before the configuration is used.
	 */
	public DBSCANConfiguration() {
		this(0, 0.0, 0, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#clone()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public DBSCANConfiguration<NN, DATA> clone() {
		try {
			return (DBSCANConfiguration<NN, DATA>) super.clone();
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get the dimensionality.
	 *
	 * @return the number of elements in the data points
	 */
	public int numDimensions() {
		return M;
	}

	/**
	 * Set the dimensionality.
	 *
	 * @param m
	 *            the number of elements in the data points
	 */
	public void setNumDimensions(int m) {
		M = m;
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
