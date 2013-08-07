package org.openimaj.experiment.evaluation.cluster.analyser;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import org.openimaj.experiment.evaluation.AnalysisResult;

/**
 * Uses a decision analysis to produce the random index result
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class FScoreAnalysis implements AnalysisResult, RandomBaselineWrappable{
	private DecisionAnalysis ann;

	/**
	 * @param ann
	 */
	public FScoreAnalysis(DecisionAnalysis ann) {
		this.ann = ann;
	}
	@Override
	public String getSummaryReport() {
		return String.format("f1=%2.5f",fscore(1));
	}

	@Override
	public String getDetailReport() {
		return this.getSummaryReport();
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
	public double score() {
		return fscore(1);
	}
	
	/**
	 * @return the proportion of true decisions made as compared to all decisions made
	 */
	public double randIndex() {
		return (ann.TP + ann.TN) / (double)(ann.TP + ann.FP + ann.TN + ann.FN);
	}
	
	@Override
	public String toString() {
		return this.getSummaryReport();
	}
	/**
	 * @param beta
	 * @return the f-score which weights up or down the relative importance of a false positive against false negatives
	 */
	public double fscore(double beta){
		double beta2 = beta * beta;
		double P = ann.precision();
		double R = ann.recall();
		if(P + R == 0) return 0;
		return ((beta2 + 1) * P * R)/(beta2 * P + R);
	}

}
