package org.openimaj.experiment.evaluation.cluster.analyser;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import org.openimaj.experiment.evaluation.AnalysisResult;

/**
 * Uses a decision analysis to produce the random index result
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class RandomIndexAnalysis implements AnalysisResult, RandomBaselineWrappable{
	private DecisionAnalysis ann;

	/**
	 * @param ann
	 */
	public RandomIndexAnalysis(DecisionAnalysis ann) {
		this.ann = ann;
	}
	
	/**
	 * @return the underlying decision analysis
	 */
	public DecisionAnalysis getDecisionAnalysis(){
		return this.ann;
	}
	
	@Override
	public String toString() {
		return this.getSummaryReport();
	}
	
	@Override
	public String getSummaryReport() {
		return String.format("randIndex=%2.5f",randIndex());
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
		return randIndex();
	}
	
	/**
	 * @return the proportion of true decisions made as compared to all decisions made
	 */
	public double randIndex() {
		return (ann.TP + ann.TN) / (double)(ann.TP + ann.FP + ann.TN + ann.FN);
	}

}
