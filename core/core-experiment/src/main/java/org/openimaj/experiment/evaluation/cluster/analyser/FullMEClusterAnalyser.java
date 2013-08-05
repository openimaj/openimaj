package org.openimaj.experiment.evaluation.cluster.analyser;

/**
 * A set of measures used to evaulate clustering.
 * These metrics are taken from: http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class FullMEClusterAnalyser implements ClusterAnalyser<FullMEAnalysis>{
	
	@Override
	public FullMEAnalysis analyse(int[][] correct, int[][] estimated) {
		FullMEAnalysis ret = new FullMEAnalysis();
		ret.purity = new PurityClusterAnalyser().analyse(correct, estimated);
		ret.nmi = new NMIClusterAnalyser().analyse(correct,estimated);
		ret.decision = new DecisionClusterAnalyser().analyse(correct, estimated);
		ret.adjRandInd = new AdjustedRandomIndexClusterAnalyser().analyse(correct, estimated);
		return ret;
	}


}
