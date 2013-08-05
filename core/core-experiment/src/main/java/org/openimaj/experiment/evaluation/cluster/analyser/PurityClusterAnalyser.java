package org.openimaj.experiment.evaluation.cluster.analyser;

import java.util.Map;

import org.openimaj.util.pair.IntIntPair;

/**
 * Create a measure of purity
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class PurityClusterAnalyser implements ClusterAnalyser<PurityAnalysis>{

	@Override
	public PurityAnalysis analyse(int[][] correct, int[][] estimated) {
		Map<Integer,Integer> invCor = ClusterAnalyserUtils.invert(correct);
		Map<Integer,Integer> invEst = ClusterAnalyserUtils.invert(estimated);
		PurityAnalysis ret = new PurityAnalysis();
		double sumPurity = 0;
		for (int k = 0; k < estimated.length; k++) {
			IntIntPair maxClassCount = ClusterAnalyserUtils.findMaxClassCount(estimated[k],invCor);
			sumPurity+=maxClassCount.second;
		}
		ret.purity = sumPurity/invEst.size();
		return ret;
	}

	

}
