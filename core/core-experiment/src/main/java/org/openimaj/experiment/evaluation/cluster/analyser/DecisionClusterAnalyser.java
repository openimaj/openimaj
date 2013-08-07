package org.openimaj.experiment.evaluation.cluster.analyser;

import java.util.Map;

import org.openimaj.experiment.evaluation.AnalysisResult;

import gov.sandia.cognition.math.MathUtil;

/**
 * Gather the true positives, true negatives, false positives, false negatives for a {@link DecisionAnalysis} instance
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T> the type of analysis
 */
public abstract class DecisionClusterAnalyser<T extends AnalysisResult> implements ClusterAnalyser<T> {

	@Override
	public T analyse(int[][] correct, int[][] estimated) {
		DecisionAnalysis ret = new DecisionAnalysis();
		Map<Integer,Integer> invCor = ClusterAnalyserUtils.invert(correct);
		long positive = 0;
		long negative = 0;
		long remainingTotal = 0;
		long[] remaining = new long[correct.length];
		long[] classCount = new long[correct.length];
		// Count the remaining items not yet clustered, and the remaining items in each class not yet clustered
		for (int i = 0; i < correct.length; i++) {
			remainingTotal += correct[i].length;
			remaining[i] = correct[i].length;
		}
		ret.TP = ret.FN = 0;
		// Go through each estimated class
		for (int[] cluster : estimated) {
			// The potential correct pairings is calculated as a cluster length pick 2
			if(cluster.length > 1)
				positive += MathUtil.binomialCoefficient(cluster.length, 2);
			remainingTotal -= cluster.length;
			// The potential negative pairings is the size of this class times the remaining items
			negative += remainingTotal  * cluster.length;

			// We count the number of each class contained in this cluster
			for (int i : cluster) {
				Integer integer = invCor.get(i);
				if(integer  == null) continue;
				classCount[integer]++;
			}
			// For each class, if its count is more than one update the true positives.
			// calculate the false negative pairings by considering the number for this class NOT in this cluster
			for (int i = 0; i < classCount.length; i++) {
				if(classCount[i] > 1){
					ret.TP += MathUtil.binomialCoefficient((int)classCount[i], 2);
				}
				remaining[i] -= classCount[i];
				ret.FN += remaining[i] * classCount[i];
				classCount[i] = 0; // reset the class count
			}
		}
		ret.FP = positive - ret.TP;
		ret.TN = negative - ret.FN;
		
		return analysisResults(ret);
	}

	/**
	 * Given a {@link DecisionAnalysis}, construct an analysis
	 * @param ret
	 * @return an analysis
	 */
	public abstract T analysisResults(DecisionAnalysis ret) ;

}
