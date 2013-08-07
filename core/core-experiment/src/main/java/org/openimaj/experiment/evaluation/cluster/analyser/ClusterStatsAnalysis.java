package org.openimaj.experiment.evaluation.cluster.analyser;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import org.openimaj.experiment.evaluation.AnalysisResult;

/**
 * Counts some overall statistics of cluster evaluation such as min, max and average cluster length
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class ClusterStatsAnalysis implements AnalysisResult{
	static class ClusterStats{
		int max;
		int min;
		int nc;
		double mean;
		int total;
		
		@Override
		public String toString() {
			return String.format("nc=%d,t=%d,mi=%d,ma=%d,me=%2.2f",nc,total,min,max,mean);
		}
	}
	
	/**
	 * Stats regarding the correct or baseline clusters
	 */
	public ClusterStats correct;
	/**
	 * Stats regarding the estimated clusters
	 */
	public ClusterStats estimated; 
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
		return String.format("correct={%s},estimated={%s}",correct,estimated);
	}

	@Override
	public String getDetailReport() {
		return this.getSummaryReport();
	}
	
	@Override
	public String toString() {
		return getSummaryReport();
	}
}
