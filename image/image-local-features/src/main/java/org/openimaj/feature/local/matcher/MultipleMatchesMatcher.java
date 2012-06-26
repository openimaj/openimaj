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
import org.openimaj.knn.approximate.ByteNearestNeighboursKDTree;
import org.openimaj.util.pair.Pair;

/**
 * A {@link LocalFeatureMatcher} that only matches points that
 * are self similar with other points. 
 * 
 * Target points that match have a distance less than a threshold
 * to the query point. The number of points less than the threshold
 * must be greater than the limit to be counted as matches. 
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T> Type of {@link Keypoint} being matched
 */
public class MultipleMatchesMatcher<T extends Keypoint> implements LocalFeatureMatcher<T> {
	private int count;
	protected List <Pair<T>> matches;
	private ByteNearestNeighboursKDTree modelKeypointsKNN;
	private double thresh;
	private List<T> modelKeypoints;
	
	/**
	 * Construct with the given minimum number of similar features
	 * and threshold for defining similarity. 
	 * @param count number of matches with a distance less than thresh to be counted.
	 * @param thresh the threshold.
	 */
	public MultipleMatchesMatcher(int count, double thresh) {
		this.count = count;
		if(this.count < 2) this.count = 2;
		this.thresh = thresh;
		matches = new ArrayList<Pair<T>>();
	}
	
	@Override
	public void setModelFeatures(List<T> modelkeys) {
		this.modelKeypoints = modelkeys;
		byte [][] data = new byte[modelkeys.size()][];
		for (int i=0; i<modelkeys.size(); i++)
			data[i] = modelkeys.get(i).ivec;
		
		modelKeypointsKNN = new ByteNearestNeighboursKDTree(data, 1, 100);
	}

	@Override
	public boolean findMatches(List<T> keys1) {
		byte [][] data = new byte[keys1.size()][];
		for (int i=0; i<keys1.size(); i++)
			data[i] = keys1.get(i).ivec;
		
		int [][] argmins = new int[keys1.size()][this.count];
		float [][] mins = new float[keys1.size()][this.count];
		
		modelKeypointsKNN.searchKNN(data, this.count, argmins, mins);
		double threshProp = (1.0 + thresh) * (1.0 + thresh) ;
		
		for (int i=0; i<keys1.size(); i++) {
			// Get the first distance

			boolean matchesMultiple = true;
			if(mins[i].length > 0 && mins[i].length >= this.count) {
				double distsq1 = mins[i][0];
				
				for (int j = 1; j < this.count; j++) {
					double distsq2 = mins[i][j];
					
					if (distsq2 > distsq1 * threshProp) {
						// Then there is a mismatch within the first this.count, break
						matchesMultiple = false;
						break;
				    }
				}
			} else {
				matchesMultiple = false;
			}
			
			if(matchesMultiple) {
				// Add each of the pairs that match
				for (int j = 0; j < this.count; j++) {
					matches.add(new Pair<T>(keys1.get(i), modelKeypoints.get(argmins[i][j])));
				}
			}
		}
		
		return true;
	}

	@Override
	public List<Pair<T>> getMatches() {
		return this.matches;
	}

}
