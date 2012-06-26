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
package org.openimaj.feature.local.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.knn.approximate.ByteNearestNeighboursKDTree;
import org.openimaj.util.pair.Pair;


/**
 * Basic keypoint matcher. Matches keypoints by finding closest
 * Two keypoints to target and checking whether the distance
 * between the two matches is sufficiently large. Allows for a match limit to be specified
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public class FastLimitedEuclideanKeypointMatcher<T extends Keypoint> implements LocalFeatureMatcher<T> {
	private ByteNearestNeighboursKDTree modelKeypointsKNN;
	private int limit;
	private List<Pair<T>> matches;
	private List<T> modelKeypoints;
	
	/**
	 * Number of matches allowed
	 * @param limit
	 */
	public FastLimitedEuclideanKeypointMatcher(int limit) {
		this.limit = limit;
	}

	@Override
	public void setModelFeatures(List<T> modelkeys) {
		modelKeypoints = modelkeys;
		
		byte [][] data = new byte[modelkeys.size()][];
		for (int i=0; i<modelkeys.size(); i++)
			data[i] = modelkeys.get(i).ivec;
		
		modelKeypointsKNN = new ByteNearestNeighboursKDTree(data, 8, 768);
	}

	class WPair extends Pair<T> implements Comparable<WPair> {
		float weight;

		public WPair(T obj1, T obj2, float weight) {
			super(obj1, obj2);
			this.weight = weight;
		}

		@Override
		public int compareTo(WPair o) {
			if (weight == o.weight) return 0;
			if (weight < o.weight) return -1;
			return 1;
		}
	}
	
	@Override
	public boolean findMatches(List<T> keys1) {
		Queue<WPair> mq = new PriorityQueue<WPair>();
		
		byte [][] data = new byte[keys1.size()][];
		for (int i=0; i<keys1.size(); i++)
			data[i] = keys1.get(i).ivec;
		
		int [] argmins = new int[keys1.size()];
		float [] mins = new float[keys1.size()];
		modelKeypointsKNN.searchNN(data, argmins, mins);
		
		for (int i=0; i<keys1.size(); i++) {
			float distsq = mins[i];
			
			mq.add(new WPair(keys1.get(i), modelKeypoints.get(argmins[i]), distsq));
		}
		
		matches = new ArrayList<Pair<T>>(limit);
		for (int i=0; i<limit; i++) matches.add(mq.poll());
		
	    return true;
	}

	@Override
	public List<Pair<T>> getMatches() {
		return matches;
	}

}
