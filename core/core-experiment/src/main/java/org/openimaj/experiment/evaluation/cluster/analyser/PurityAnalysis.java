package org.openimaj.experiment.evaluation.cluster.analyser;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import org.openimaj.experiment.evaluation.AnalysisResult;

/**
 * A measure of how pure each cluster is.
 * P = 1/N Sigma_k max_j | w_k AND c_j |
 *
 * Count the true classes of all the elements in a class, make a count of the largest group from each cluster,
 * divide by number of elements in all clusters.
 *
 * High means: most of the clusters had a high number of a single class
 * Low means: most of the clusters had a roughly equal spread of all the classes
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class PurityAnalysis implements AnalysisResult,RandomBaselineWrappable{

	/**
	 * the purity
	 */
	public double purity;

	@Override
	public JasperPrint getSummaryReport(String title, String info) throws JRException {
		throw new UnsupportedOperationException();
	}

	@Override
	public JasperPrint getDetailReport(String title, String info) throws JRException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSummaryReport() {
		return toString();
	}

	@Override
	public String getDetailReport() {
		return toString();
	}
	
	@Override
	public String toString() {
		return String.format("purity=%2.4f",purity);
	}

	@Override
	public double score() {
		return this.purity;
	}

}
