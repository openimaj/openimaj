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
package org.openimaj.experiment.evaluation.cluster.analyser;

import gnu.trove.map.hash.TIntIntHashMap;

import java.util.HashMap;
import java.util.Map;

import org.openimaj.util.pair.IntIntPair;

/**
 * Tools for {@link ClusterAnalyser} instances
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class ClusterAnalyserUtils{
	/**
	 * Map index to cluster from cluster array
	 * @param clustered
	 * @return index to cluster
	 */
	public static Map<Integer, Integer> invert(int[][] clustered) {
		int cluster = 0;
		Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
		for (int[] is : clustered) {
			for (int index : is) {
				ret.put(index, cluster);
			}
			cluster++;
		}
		return ret;
	}

	/**
	 * @param is
	 * @param invCor
	 * @return the cluster with the maximum
	 */
	public static IntIntPair findMaxClassCount(int[] is, Map<Integer, Integer> invCor) {
		TIntIntHashMap classCounts = new TIntIntHashMap();
		int max = 0;
		int c = 0;
		for (int i : is) {
			Integer c_i = invCor.get(i);
			if(c_i == null) continue;
			int count = classCounts.adjustOrPutValue(c_i, 1, 1);
			if(count > max){
				max = count;
				c = c_i;
			}
		}
		return IntIntPair.pair(c, max);
	}

}
