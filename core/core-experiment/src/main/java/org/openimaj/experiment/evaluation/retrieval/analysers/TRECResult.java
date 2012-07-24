package org.openimaj.experiment.evaluation.retrieval.analysers;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import org.openimaj.experiment.evaluation.AnalysisResult;

/**
 * An {@link AnalysisResult} wrapping the output of the
 * trec_eval tool.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class TRECResult implements AnalysisResult {
	String trecOutput;
	
	/**
	 * Construct with the given output from trec_eval
	 * @param trecOutput the output
	 */
	public TRECResult(String trecOutput) {
		this.trecOutput = trecOutput;
	}

	@Override
	public String toString() {
		return trecOutput;
	}

	@Override
	public JasperPrint getSummaryReport(String title, String info) throws JRException {
		//FIXME
		throw new UnsupportedOperationException();
	}

	@Override
	public JasperPrint getDetailReport(String title, String info) throws JRException {
		//FIXME
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSummaryReport() {
		return trecOutput;
	}

	@Override
	public String getDetailReport() {
		return trecOutput;
	}
}
