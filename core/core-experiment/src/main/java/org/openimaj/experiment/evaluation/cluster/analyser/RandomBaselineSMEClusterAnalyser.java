package org.openimaj.experiment.evaluation.cluster.analyser;

/**
 * A set of measures used to evaulate clustering.
 * These metrics are taken from: http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class RandomBaselineSMEClusterAnalyser implements ClusterAnalyser<RandomBaselineSMEAnalysis>{
	
	@Override
	public RandomBaselineSMEAnalysis analyse(int[][] correct, int[][] estimated) {
		RandomBaselineSMEAnalysis ret = new RandomBaselineSMEAnalysis(correct,estimated);
		ret.stats = new ClusterStatsAnalyser().analyse(correct,estimated);
		ret.purity = new RandomBaselineClusterAnalyser<PurityClusterAnalyser, PurityAnalysis>(new PurityClusterAnalyser()).analyse(correct, estimated);
		ret.randIndex = new RandomBaselineClusterAnalyser<RandomIndexClusterAnalyser, RandomIndexAnalysis>(new RandomIndexClusterAnalyser()).analyse(correct, estimated);
		ret.fscore = new RandomBaselineClusterAnalyser<FScoreClusterAnalyser, FScoreAnalysis>(new FScoreClusterAnalyser()).analyse(correct, estimated);
		return ret;
	}


}
