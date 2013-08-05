package org.openimaj.experiment.evaluation.cluster.analyser;

import gnu.trove.map.hash.TIntIntHashMap;

import java.util.HashMap;
import java.util.Map;

import org.openimaj.experiment.evaluation.AnalysisResult;
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
			int count = classCounts.adjustOrPutValue(c_i, 1, 1);
			if(count > max){
				max = count;
				c = c_i;
			}
		}
		return IntIntPair.pair(c, max);
	}

}
