package org.openimaj.experiment.evaluation.cluster.analyser;

/**
 * A set of measures used to evaluate clustering quality. These metrics are
 * taken from:
 * {@link "http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html"}
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class RandomBaselineSMEClusterAnalyser implements ClusterAnalyser<RandomBaselineSMEAnalysis> {
	/**
	 * Default constructor
	 */
	public RandomBaselineSMEClusterAnalyser() {
	}

	@Override
	public RandomBaselineSMEAnalysis analyse(int[][] correct, int[][] estimated) {
		final RandomBaselineSMEAnalysis ret = new RandomBaselineSMEAnalysis(correct, estimated);

		ret.stats = new ClusterStatsAnalyser().analyse(correct, estimated);

		ret.purity = new RandomBaselineClusterAnalyser<PurityClusterAnalyser, PurityAnalysis>(new PurityClusterAnalyser())
				.analyse(correct, estimated);

		ret.randIndex = new RandomBaselineClusterAnalyser<RandomIndexClusterAnalyser, RandomIndexAnalysis>(
				new RandomIndexClusterAnalyser()).analyse(correct, estimated);

		ret.fscore = new RandomBaselineClusterAnalyser<FScoreClusterAnalyser, FScoreAnalysis>(new FScoreClusterAnalyser())
				.analyse(correct, estimated);

		return ret;
	}
}
