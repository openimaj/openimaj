package org.openimaj.experiment.evaluation.cluster.analyser;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class RandomIndexClusterAnalyser extends DecisionClusterAnalyser<RandomIndexAnalysis>{

	@Override
	public RandomIndexAnalysis analysisResults(DecisionAnalysis ret) {
		return new RandomIndexAnalysis(ret);
	}

}
