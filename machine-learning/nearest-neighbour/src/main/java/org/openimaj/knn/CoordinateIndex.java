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
package org.openimaj.knn;

import java.util.Collection;

import org.openimaj.math.geometry.point.Coordinate;

/**
 * Interface representing an index of {@link Coordinate}s that can
 * have points added to it and can be searched in a variety of ways.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> the type of coordinate.
 */
public interface CoordinateIndex<T extends Coordinate> {
	/**
	 * Insert a single coordinate into the index.
	 * @param point coordinate to add.
	 */
	public void insert(T point);
	
	/**
	 * Search for all the coordinates in the index that
	 * lie in the hyper-rectangle defined by the lower and
	 * upper bound coordinates. Store found coordinates
	 * in the results collection.
	 * 
	 * @param results Collection to hold the found coordinates.
	 * @param lowerExtreme Lower bound of the hyper-rectangle.
	 * @param upperExtreme Upper bound of the hyper-rectangle.
	 */
	public void rangeSearch(Collection<T> results, Coordinate lowerExtreme, Coordinate upperExtreme);
	
	/**
	 * Find the nearest coordinate in the index to the query
	 * coordinate.
	 * 
	 * @param query The query coordinate.
	 * @return the closest coordinate in the index.
	 */
	public T nearestNeighbour(Coordinate query);
	
	/**
	 * Find the k nearest neighbour points in the index to
	 * the query coordinate. Store found coordinates
	 * in the results collection.
	 * 
	 * @param result Collection to hold the found coordinates.
	 * @param query The query coordinate.
	 * @param k The number of neighbours to find.
	 */
	public void kNearestNeighbour(Collection<T> result, Coordinate query, int k);
}
