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
import java.util.Collections;
import java.util.List;

import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.util.pair.Pair;


/**
 * Matcher rejects matches with no local support
 * 
 * @author Jonathon Hare
 * @param <T> 
 *
 */
public class VotingKeypointMatcher<T extends Keypoint> extends FastBasicKeypointMatcher<T> implements LocalFeatureMatcher<T> {
	int neighbours;
	List<Pair<T>> consistentMatches = new ArrayList<Pair<T>>();
	protected int minVote;
	protected float singularityDistance;
	
	/**
	 * @param threshold threshold for determining matching keypoints
	 */
	public VotingKeypointMatcher(int threshold) {
		this(threshold, 15, 1, 200.0f); //default to 15 as in VideoGoogle paper
	}
	
	/**
	 * @param threshold threshold for determining matching keypoints
	 * @param neighbours number of neighbours within which to check for local support
	 * @param minVote
	 * @param singularityDistance
	 */
	public VotingKeypointMatcher(int threshold, int neighbours, int minVote, float singularityDistance) {
		super(threshold);
		this.neighbours = neighbours;
		this.minVote = minVote;
		this.singularityDistance = singularityDistance;
	}
	
	/**
	 * @return a list of consistent matching keypoints according
	 * to the estimated model parameters.
	 */
	@Override
	public List<Pair<T>> getMatches() {
		return consistentMatches;
	}

	/**
	 * @return a list of all matches irrespective of whether they fit the model
	 */
	public List<Pair<T>> getAllMatches() {
		return matches;
	}

	@Override
	public boolean findMatches(List<T> keys1) {
		super.findMatches(keys1);
		
		consistentMatches = new ArrayList<Pair<T>>();
		
		//filter dups
		//matches = ConsistentKeypointMatcher.filterColinear(matches, 1);
		
		//filter spurious matches by voting
		for (Pair<T> match : matches) {
			int vote = vote(match);
			if (vote > minVote)
				consistentMatches.add(match);
		}
		
		if (consistentMatches.size() == 0) 
			return false;
		
		//reject mappings to very close points
		if (checkSingularity()) {
			consistentMatches.clear();
			return false;
		}
		
		return true;
	}
	
	protected float [] getCentroid() {
		float mx = consistentMatches.get(0).secondObject().x;
		float my = consistentMatches.get(0).secondObject().y;
		for (int i=1; i<consistentMatches.size(); i++) {
			mx += consistentMatches.get(i).secondObject().x;
			my += consistentMatches.get(i).secondObject().y;
		}
		return new float[] {mx/consistentMatches.size(), my/consistentMatches.size()};
	}
	
	protected boolean checkSingularity() {
		float [] centroid = getCentroid();
		Keypoint k = new Keypoint();
		k.y = centroid[1];
		k.x = centroid[0];
		
		for (Pair<T> p : consistentMatches) {
			if (euclideanSqr(p.secondObject(), k) > singularityDistance) return false;
		}
		return true;
	}
	
	protected int vote(Pair<T> match) {
		List<T> nn = findModelNeighbours(match.secondObject());
		int vote = 0;
		
		for (Pair<T> m : matches) {
			for (Keypoint k : nn) {
				if (m.secondObject() == k) {
					vote++;
					break;
				}
			}
		}
		return vote;
	}
	
	protected float euclideanSqr(Keypoint k1, Keypoint k2) {
		return ((k1.x - k2.x) * (k1.x - k2.x)) + 
				((k1.y - k2.y)*(k1.y - k2.y));		
	}
	
	protected List<T> findModelNeighbours(final T kp) {
		class KpDist<Q extends Keypoint> implements Comparable<KpDist<Q>> {
			float distance;
			T keypoint;
			
			KpDist(T keypoint) {
				this.keypoint = keypoint;
				
				distance = euclideanSqr(keypoint, kp);
			}

			@Override
			public int compareTo(KpDist<Q> o) {
				if (distance > o.distance) return 1;
				if (distance < o.distance) return -1;
				return 0;
			}
		}

		List<KpDist<T>> list = new ArrayList<KpDist<T>>(); 
		for (T k : modelKeypoints) {
			list.add(new KpDist<T>(k));
		}
		Collections.sort(list);
		
		List<T> keys = new ArrayList<T>();
		for (int i=0; i<Math.min(neighbours, list.size()); i++)
			keys.add(list.get(i).keypoint);
		
		return keys;
	}
}
