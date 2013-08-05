package org.openimaj.experiment.evaluation.cluster.analyser;

/**
 * A set of measures used to evaulate clustering.
 * These metrics are taken from: http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SimpleMEClusterAnalyser implements ClusterAnalyser<SimpleMEAnalysis>{
	
	@Override
	public SimpleMEAnalysis analyse(int[][] correct, int[][] estimated) {
		SimpleMEAnalysis ret = new SimpleMEAnalysis();
		ret.purity = new PurityClusterAnalyser().analyse(correct, estimated);
		ret.decision = new DecisionClusterAnalyser().analyse(correct, estimated);
		ret.adjRandInd = new AdjustedRandomIndexClusterAnalyser().analyse(correct, estimated);
		return ret;
	}


}
