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
 * 
 * Uses a ByteKDTree to estimate approximate nearest neighbours more
 * efficiently.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <T>
 */
public class FastEuclideanKeypointMatcher<T extends Keypoint> implements LocalFeatureMatcher<T> {
	private ByteNearestNeighboursKDTree modelKeypointsKNN;
	private int threshold;
	protected List<Pair<T>> matches;
	private List<T> modelKeypoints;

	/**
	 * @param threshold
	 *            threshold for determining matching keypoints
	 */
	public FastEuclideanKeypointMatcher(int threshold) {
		this.threshold = threshold;
	}

	@Override
	public void setModelFeatures(List<T> modelkeys) {
		modelKeypoints = modelkeys;

		final byte[][] data = new byte[modelkeys.size()][];
		for (int i = 0; i < modelkeys.size(); i++)
			data[i] = modelkeys.get(i).ivec;

		modelKeypointsKNN = new ByteNearestNeighboursKDTree(data, 8, 768);
	}

	@Override
	public boolean findMatches(List<T> keys1) {
		matches = new ArrayList<Pair<T>>();

		final byte[][] data = new byte[keys1.size()][];
		for (int i = 0; i < keys1.size(); i++)
			data[i] = keys1.get(i).ivec;

		final int[] argmins = new int[keys1.size()];
		final float[] mins = new float[keys1.size()];
		modelKeypointsKNN.searchNN(data, argmins, mins);

		for (int i = 0; i < keys1.size(); i++) {
			final float distsq = mins[i];

			if (distsq < threshold) {
				matches.add(new Pair<T>(keys1.get(i), modelKeypoints.get(argmins[i])));
			}
		}

		return true;
	}

	@Override
	public List<Pair<T>> getMatches() {
		return this.matches;
	}

	/**
	 * Set the matching threshold
	 * 
	 * @param threshold
	 *            the threshold
	 */
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}
}
