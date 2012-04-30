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

import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.util.pair.Pair;


/**
 * Basic keypoint matcher. Matches keypoints by finding closest
 * Two keypoints to target and checking whether the distance
 * between the two matches is sufficiently large.
 * 
 * @author Jonathon Hare
 * @param <T> Keypoint type
 *
 */
public class FastLimitedBasicKeypointMatcher<T extends Keypoint> extends FastBasicKeypointMatcher<T> {
	private int limit;

	/**
	 * 
	 * @param threshold threshold for determining matching keypoints
	 * @param limit
	 */
	public FastLimitedBasicKeypointMatcher(int threshold, int limit)
	{
		super(threshold);
		this.limit = limit;
	}
	
	@Override
	public boolean findMatches(List<T> keys1)
	{
		matches = new ArrayList<Pair<T>>();
		
		byte [][] data = new byte[keys1.size()][];
		for (int i=0; i<keys1.size(); i++)
			data[i] = keys1.get(i).ivec;
		
		int [][] argmins = new int[keys1.size()][2];
		float [][] mins = new float[keys1.size()][2];
		modelKeypointsKNN.searchKNN(data, 2, argmins, mins);
		
		for (int i=0; i<keys1.size(); i++) {
			float distsq1 = mins[i][0];
			float distsq2 = mins[i][1];
			
			if (10 * 10 * distsq1 < thresh * thresh * distsq2) {
				matches.add(new Pair<T>(keys1.get(i), modelKeypoints.get(argmins[i][0])));
		    }
			
			if (matches.size() >= limit) break;
		}
		
	    return true;
	}
}
