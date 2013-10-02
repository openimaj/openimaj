package org.openimaj.experiment.evaluation.cluster.analyser;

/**
 * A set of measures used to evaluate clustering. These metrics are taken from:
 * {@link "http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html"}
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class SimpleMEClusterAnalyser implements ClusterAnalyser<SimpleMEAnalysis> {
	@Override
	public SimpleMEAnalysis analyse(int[][] correct, int[][] estimated) {
		final SimpleMEAnalysis ret = new SimpleMEAnalysis();
		ret.purity = new PurityClusterAnalyser().analyse(correct, estimated);
		ret.randIndex = new RandomIndexClusterAnalyser().analyse(correct, estimated);
		ret.decision = ret.randIndex.getDecisionAnalysis();
		ret.fscore = new FScoreAnalysis(ret.randIndex.getDecisionAnalysis());
		ret.adjRandInd = new AdjustedRandomIndexClusterAnalyser().analyse(correct, estimated);
		return ret;
	}
}
