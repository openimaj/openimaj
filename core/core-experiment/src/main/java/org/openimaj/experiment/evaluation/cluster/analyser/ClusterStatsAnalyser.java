package org.openimaj.experiment.evaluation.cluster.analyser;

import org.openimaj.experiment.evaluation.cluster.analyser.ClusterStatsAnalysis.ClusterStats;

/**
 * Analysers extracts stats of the correct and estimated clusterings
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class ClusterStatsAnalyser implements ClusterAnalyser<ClusterStatsAnalysis>{

	@Override
	public ClusterStatsAnalysis analyse(int[][] correct, int[][] estimated) {
		ClusterStatsAnalysis ret = new ClusterStatsAnalysis();
		ret.correct = stats(correct);
		ret.estimated = stats(estimated);
		return ret;
	}

	private ClusterStats stats(int[][] correct) {
		ClusterStats ret = new ClusterStats();
		ret.max = 0;
		ret.min = Integer.MAX_VALUE;
		ret.mean = 0;
		
		for (int[] is : correct) {
			ret.max = Math.max(ret.max, is.length);
			ret.min = Math.min(ret.min, is.length);
			ret.mean += is.length / (double)correct.length;
			ret.total += is.length;
			ret.nc ++;
		}
		return ret;
	}

}
