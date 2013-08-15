package org.openimaj.experiment.evaluation.cluster.analyser;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import org.openimaj.experiment.evaluation.AnalysisResult;

/**
 * Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class RandomBaselineSMEAnalysis implements AnalysisResult{

	/**
	 * A measure of how pure each cluster is.
	 * P = 1/N Sigma_k max_j | w_k AND c_j |
	 *
	 * Count the true classes of all the elements in a class, make a count of the largest group from each cluster,
	 * divide by number of elements in all clusters.
	 *
	 * High means: most of the clusters had a high number of a single class
	 * Low means: most of the clusters had a roughly equal spread of all the classes
	 */
	public RandomBaselineClusterAnalysis<PurityAnalysis> purity;
	
	/**
	 * 
	 */
	public RandomBaselineClusterAnalysis<FScoreAnalysis> fscore;
	
	/**
	 * 
	 */
	public RandomBaselineClusterAnalysis<RandomIndexAnalysis> randIndex;

	/**
	 * General statistics
	 */
	public ClusterStatsAnalysis stats;

	/**
	 * The correct clustering
	 */
	public int[][] correct;

	/**
	 * The estimation
	 */
	public int[][] estimated;


	public RandomBaselineSMEAnalysis(int[][] correct, int[][] estimated) {
		this.correct = correct;
		this.estimated = estimated;
	}

	@Override
	public String getSummaryReport() {
		return String.format("s=[%s],p=[%s],f1=[%s],r=[%s])",stats,purity,fscore,randIndex);
	}

	@Override
	public String getDetailReport() {
		return String.format("stats: [%s]\npurity: [%s]\nf1: [%s]\nrandIndex: [%s])",stats,purity,fscore,randIndex);
	}
	
	@Override
	public String toString() {
		return getDetailReport();
	}


	@Override
	public JasperPrint getSummaryReport(String title, String info) throws JRException {throw new UnsupportedOperationException();}

	@Override
	public JasperPrint getDetailReport(String title, String info)throws JRException {throw new UnsupportedOperationException();}

}