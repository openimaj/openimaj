package org.openimaj.experiment.evaluation.cluster.analyser;

import org.openimaj.experiment.evaluation.AnalysisResult;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

/**
 * The result of a {@link TestRandomBaselineClusterAnalyser} which wraps the
 * baseline result and result of an AnalysisResult
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public class RandomBaselineClusterAnalysis<T extends RandomBaselineWrappable & AnalysisResult> implements AnalysisResult,RandomBaselineWrappable{

	private T score;
	private double randscore;

	/**
	 * @param score
	 * @param randscore
	 */
	public RandomBaselineClusterAnalysis(T score, double randscore) {
		this.score = score;
		this.randscore = randscore;
	}

	@Override
	public JasperPrint getSummaryReport(String title, String info) throws JRException {
		throw new UnsupportedOperationException();
	}

	@Override
	public JasperPrint getDetailReport(String title, String info)throws JRException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSummaryReport() {
		return String.format("s=(%s),b=%2.5f,d=%2.4f",this.score.getSummaryReport(),this.randscore,score());
	}

	@Override
	public String getDetailReport() {
		return getSummaryReport();
	}
	
	@Override
	public String toString() {
		return this.getSummaryReport();
	}
	
	@Override
	public double score() {
		return this.score.score() - this.randscore;
	}
	
	/**
	 * @return the unmodified analysis
	 */
	public T getUnmodified(){
		return this.score;
	}

}
