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
package org.openimaj.ml.clustering.assignment;

/**
 * The {@link HardAssigner} interface describes classes that assign a spatial
 * point to a single cluster.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <DATATYPE>
 *            the primitive array datatype which represents a centroid of this
 *            cluster.
 * @param <DISTANCES>
 *            primitive array datatype for recording distances between points
 *            and cluster centroids.
 * @param <DISTANCE_INDEX>
 *            datatype for representing an <index, distance> pair.
 */
public interface HardAssigner<DATATYPE, DISTANCES, DISTANCE_INDEX> extends Assigner<DATATYPE> {
	/**
	 * Assign data to a cluster.
	 * 
	 * @param data
	 *            the data.
	 * @return The cluster indices which the data was assigned to.
	 */
	public abstract int[] assign(final DATATYPE[] data);

	/**
	 * Assign a single point to a cluster.
	 * 
	 * @param data
	 *            datum to assign.
	 * 
	 * @return the cluster index.
	 */
	public abstract int assign(final DATATYPE data);

	/**
	 * Assign data to clusters. The results are returned in the indices and
	 * distances arrays. The return arrays must have the same length as the data
	 * array.
	 * 
	 * @param data
	 *            the data.
	 * @param indices
	 *            the cluster index for each data point.
	 * @param distances
	 *            the distance to the closest cluster for each data point.
	 */
	public abstract void assignDistance(final DATATYPE[] data, int[] indices, DISTANCES distances);

	/**
	 * Assign a single point to a cluster.
	 * 
	 * @param data
	 *            point to assign.
	 * 
	 * @return the cluster index and distance.
	 */
	public abstract DISTANCE_INDEX assignDistance(final DATATYPE data);

	/**
	 * The number of centroids or unique ids that can be generated.
	 * 
	 * @return The number of centroids or unique ids that can be generated.
	 */
	public int size();
}
