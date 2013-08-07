package org.openimaj.experiment.evaluation.cluster;

import java.util.HashMap;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import org.openimaj.experiment.evaluation.AnalysisResult;

/**
 *
 * @param <KEY> the key of the experiment range
 * @param <ANA> the analysis at that key
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class RangedAnalysisResult<KEY, ANA extends AnalysisResult> extends HashMap<KEY,ANA> implements AnalysisResult{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8374344456749228231L;

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
		String str = "";
		for (java.util.Map.Entry<KEY, ANA> ent : this.entrySet()) {
			str += String.format("%s: %s\n",ent.getKey(),ent.getValue());
		}
		return str;
	}

	@Override
	public String getDetailReport() {
		return getSummaryReport();
	}

}
