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

import org.openimaj.util.pair.IndependentPair;

/**
 * The {@link SoftAssigner} interface describes classes that
 * assign a spatial point to multiple clusters, possibly with 
 * weighting.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <DATATYPE> the primitive array datatype which represents a centroid of this cluster.
 * @param <DISTANCES> primitive array datatype for recording distances between points and cluster centroids.
 */
public interface SoftAssigner<DATATYPE, DISTANCES> extends Assigner<DATATYPE> {
	/**
	 * Assign data to clusters.
	 * 
	 * @param data the data.
	 * @return The cluster indices which the data was assigned to.
	 */
	public int[][] assign(DATATYPE[] data);

	/**
	 * Assign a single point to some clusters.
	 * 
	 * @param data datum to assign.
	 * 
	 * @return the assigned cluster indices.
	 */
	public int[] assign(DATATYPE data);
	
	/**
	 * Assign data to clusters. The results are returned
	 * in the indices and distances arrays. The return arrays
	 * must have the same length as the data array. 
	 *            
	 * @param data the data.
	 * @param assignments the cluster indices for each data point.
	 * @param weights the weights to the for each cluster for each data point.
	 */
	public void assignWeighted(DATATYPE[] data, int[][] assignments, DISTANCES[] weights);

	/**
	 * Assign a single point to some clusters.
	 * 
	 * @param data point to assign.
	 * 
	 * @return the assigned cluster indices and weights.
	 */
	public IndependentPair<int[], DISTANCES> assignWeighted(DATATYPE data);
}
