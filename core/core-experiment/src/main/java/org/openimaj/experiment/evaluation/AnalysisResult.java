package org.openimaj.experiment.evaluation;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

/**
 * The result of the analysis of raw data. 
 * <p>
 * {@link AnalysisResult}s are capable of producing summary and
 * detailed information in the form of a {@link String} as well
 * as producing a {@link JasperPrint} objects containing a renderable
 * summary and detailed reports.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public interface AnalysisResult {
	/**
	 * Get a {@link JasperPrint} summarising the result.
	 * @param title a title to add to the report
	 * @param info information to print at the beginning of the report
	 * 
	 * @return a {@link String} summarising the result
	 * @throws JRException 
	 */
	public JasperPrint getSummaryReport(String title, String info) throws JRException;
	
	/**
	 * Get a {@link JasperPrint} detailing the result.
	 * @param title a title to add to the report
	 * @param info information to print at the beginning of the report
	 * 
	 * @return a {@link JasperPrint} detailing the result.
	 * @throws JRException 
	 */
	public JasperPrint getDetailReport(String title, String info) throws JRException;
	
	/**
	 * Get a {@link String} summarising the result.
	 * 
	 * @return a {@link String} summarising the result
	 */
	public String getSummaryReport();
	
	/**
	 * Get a {@link String} detailing the result.
	 * 
	 * @return a {@link String} detailing the result
	 */
	public String getDetailReport();
}
