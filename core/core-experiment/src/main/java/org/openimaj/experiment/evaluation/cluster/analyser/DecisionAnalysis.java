package org.openimaj.experiment.evaluation.cluster.analyser;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import org.openimaj.experiment.evaluation.AnalysisResult;

/**
 * Counting the number of true positives, true negatives, flase postitives and false negatives
 * one can produce various cluster quality metrics including the fscore and randindex
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class DecisionAnalysis implements AnalysisResult{

	/**
	 * The total number of pairs in a cluster which belong to the same class
	 */
	public long TP;
	/**
	 * The total number of pairs in a cluster which do not belong to the same class
	 */
	public long FP;
	/**
	 * The total number of pairs in different clusters which belong to different classes
	 */
	public long TN;
	/**
	 * The total number of pairs in different clusters which belong to the same class
	 */
	public long FN;

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
		return String.format("{tp=%d,fp=%d,tn=%d,fn=%d,P=%2.2f,R=%2.5f}",TP,FP,TN,FN,precision(),recall());
	}
	
	/**
	 * @return The number of true positives as a proportion of overall positives 
	 */
	public double precision(){
		if( TP + FP == 0) return 0;
		return TP / (double)(TP + FP);
	};

	/**
	 * @return The number of true positives as a porportion of true positives and false negatives
	 */
	public double recall(){
		if( TP + FN == 0) return 0;
		return TP / (double)(TP + FN);
	};

}
