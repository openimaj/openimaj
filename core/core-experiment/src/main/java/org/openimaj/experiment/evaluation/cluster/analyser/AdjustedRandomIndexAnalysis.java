package org.openimaj.experiment.evaluation.cluster.analyser;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import org.openimaj.experiment.evaluation.AnalysisResult;

/**
 * The adjsuted random index as described by: http://faculty.washington.edu/kayee/pca/supp.pdf
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class AdjustedRandomIndexAnalysis implements AnalysisResult{
	/**
	 * 
	 */
	public double adjRandInd;

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
		return String.format("AdjRandI=%2.5f",adjRandInd);
	}

}
