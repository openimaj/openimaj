package org.openimaj.experiment.evaluation.cluster.analyser;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import org.openimaj.experiment.evaluation.AnalysisResult;

/**
 * Result of applying a {@link SimpleMEClusterAnalyser}.
 * 
 * Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class SimpleMEAnalysis implements AnalysisResult {

	/**
	 * A measure of how pure each cluster is.
	 * <code>P = 1/N Sigma_k max_j | w_k AND c_j |</code>
	 * <p>
	 * Count the true classes of all the elements in a class, make a count of
	 * the largest group from each cluster, divide by number of elements in all
	 * clusters.
	 * <p>
	 * High means: most of the clusters had a high number of a single class Low
	 * means: most of the clusters had a roughly equal spread of all the classes
	 */
	public PurityAnalysis purity;

	/**
	 * The {@link DecisionAnalysis} instance
	 */
	public DecisionAnalysis decision;

	/**
	 * 
	 */
	public FScoreAnalysis fscore;

	/**
	 * 
	 */
	public RandomIndexAnalysis randIndex;

	/**
	 * 
	 */
	public AdjustedRandomIndexAnalysis adjRandInd;

	@Override
	public String getSummaryReport() {
		return String.format("(%s,%s.%s,%s,%s)", purity, decision, fscore, randIndex, adjRandInd);
	}

	@Override
	public String getDetailReport() {
		return "";
	}

	@Override
	public JasperPrint getSummaryReport(String title, String info) throws JRException {
		throw new UnsupportedOperationException();
	}

	@Override
	public JasperPrint getDetailReport(String title, String info) throws JRException {
		throw new UnsupportedOperationException();
	}

}
