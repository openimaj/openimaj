package org.openimaj.experiment.evaluation.cluster.analyser;

import org.openimaj.experiment.evaluation.AnalysisResult;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public interface ClusterAnalyser<T extends AnalysisResult> {
	/**
	 * @param correct
	 * @param estimated
	 * @return the analysis of the correct vs estimated results
	 */
	public T analyse(int[][] correct, int[][] estimated);
}
