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
package org.openimaj.ml.clustering;

import org.openimaj.data.DataSource;

/**
 * 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 * @param <T> The type which can be read and written by this cluster
 * @param <DATATYPE> the primative datatype which represents a centroid of this cluster
 */
public interface Cluster<T, DATATYPE> extends ReadWriteableCluster {
	
	/**
	 * Train clusters
	 * 
	 * @param data data.
	 * 
	 * @return -1 if an overflow may have occurred.
	 */
	public abstract int train(final DATATYPE[] data);
	
	/**
	 * Train clusters with a data source, can be more efficient
	 * 
	 * @param data data.
	 * 
	 * @return -1 if an overflow may have occurred.
	 */
	public abstract int train(DataSource<DATATYPE> data);
	
	/**
	 * Get data dimensionality
	 * @return data dimensionality.
	 */
	public abstract int getNDims();

	/**
	 * Get the number of centers K
	 * @return number of centers K.
	 */
	public int getNumberClusters() ;
	
	/**
	 * Prepare the cluster for pushing
	 * @param exact TODO
	 */
	public void optimize(boolean exact) ;
	
	/**
	 * Project data to clusters.
	 *            
	 * @param data data.
	 * @return The cluster indecies which the data was pushed to
	 */
	public abstract int[] push(final DATATYPE[] data);

	/**
	 * Project one datum to clusters
	 * 
	 * @param data datum to project.
	 * 
	 * @return the cluster index.
	 */
	public abstract int push_one(final DATATYPE data);

	/**
	 * Project data to clusters.
	 *            
	 * @param data data.
	 * @param numNeighbours number of neighboring clusters to return also. When set to 1 this is equivalent to {@link Cluster#push(DATATYPE[])}
	 * @return The centers and neighbours for the data
	 */
	public abstract int[][] push(final DATATYPE[] data, final int numNeighbours);

	/**
	 * Project one datum to clusters
	 * 
	 * @param data datum to project.
	 * @param numNeighbours number of neighbouring clusters to return also. When set to 1 this is equivalent to {@link Cluster#push_one(Object)}
	 * 
	 * @return the cluster index and the index of neighbours.
	 */
	public abstract int[] push_one(final DATATYPE data, final int numNeighbours);
	
	/**
	 * Utility function useful for testing. The cluster must return something it considers to be it's cluster
	 * centroids. Different types of cluster will clearly return different data types here. This might (and often will be)
	 * null given that it often might not make any sense. It is a sign of a good cluster that can produce a set of centroids
	 * for itself.
	 * 
	 * @return The cluster's centroids.
	 */
	public abstract DATATYPE[] getClusters();
	
}
