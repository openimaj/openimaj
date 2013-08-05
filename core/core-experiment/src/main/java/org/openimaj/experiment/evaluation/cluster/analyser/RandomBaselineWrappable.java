package org.openimaj.experiment.evaluation.cluster.analyser;

import org.openimaj.experiment.evaluation.AnalysisResult;

/**
 * An {@link AnalysisResult} which can offer some score and thus
 * be compared to a random baseline
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface RandomBaselineWrappable {
	/**
	 * @return the underlying score to wrap in a {@link TestRandomBaselineClusterAnalyser}
	 */
	public double score();
}
