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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;

import org.openimaj.math.geometry.point.Coordinate;

/**
 * Implementation of a {@link CoordinateIndex} that performs
 * searching by brute-force comparison over the indexed coordinates.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> the type of Coordinate 
 */
public class CoordinateBruteForce<T extends Coordinate> implements CoordinateIndex<T> {
	List<T> data = new ArrayList<T>();
	
	/**
	 * Default constructor. Creates an empty index.
	 */
	public CoordinateBruteForce() {}
	
	/**
	 * Construct the index and populate it with the given data.
	 * @param data the data to add to the index.
	 */
	public CoordinateBruteForce(List<T> data) {
		this.data = data;
	}
	
	@Override
	public void insert(T point) {
		data.add(point);
	}
	
	@Override
	public void rangeSearch(Collection<T> results, Coordinate lowerExtreme, Coordinate upperExtreme) {
		for (T d : data) {
			if (CoordinateKDTree.isContained(d, lowerExtreme, upperExtreme))
				results.add(d);
		}
	}
	
	@Override
	public T nearestNeighbour(Coordinate query) {
		float minDist = Float.MAX_VALUE;
		T best = null;
		
		for (T d : data) {
			float dist = CoordinateKDTree.distance(d, query);
			if (dist < minDist) {
				minDist = dist;
				best = d;
			}
		}
		
		return best;
	}

	class Match implements Comparable<Match> {
		float distance;
		T coord;
		@Override public int compareTo(Match o) {
			if (distance > o.distance) return 1;
			if (distance < o.distance) return -1;
			return 0;
		}
		@Override
		public String toString() { return distance + ""; }
	}
	
	@Override
	public void kNearestNeighbour(Collection<T> result, Coordinate query, int k) {
		PriorityQueue<Match> queue = new PriorityQueue<Match>(data.size());
		
		for (T d : data) {
			Match m = new Match();
			m.coord = d;
			m.distance = CoordinateKDTree.distance(query, d);
			queue.add(m);
		}
		
		for (int i=0; i<k; i++)
			result.add(queue.poll().coord);
	}
}
