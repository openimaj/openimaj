package org.openimaj.experiment.evaluation.cluster.analyser;

import org.openimaj.experiment.evaluation.AnalysisResult;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

/**
 * Measures normalised mutual information. Intuatively this is how much information
 * one gets about the true clustering given the estimate clustering
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class NMIAnalysis implements AnalysisResult,RandomBaselineWrappable{

	/**
	 * The NMI
	 */
	public double nmi;

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
		return String.format("nmi=%2.4f",nmi);
	}

	@Override
	public double score() {
		return nmi;
	}

}
