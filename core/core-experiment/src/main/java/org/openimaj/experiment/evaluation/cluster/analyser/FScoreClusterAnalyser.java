package org.openimaj.experiment.evaluation.cluster.analyser;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class FScoreClusterAnalyser extends DecisionClusterAnalyser<FScoreAnalysis>{

	@Override
	public FScoreAnalysis analysisResults(DecisionAnalysis ret) {
		return new FScoreAnalysis(ret);
	}

}
