package org.openimaj.experiment.evaluation.cluster.analyser;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import org.openimaj.experiment.evaluation.AnalysisResult;

/**
 * Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class MEAnalysis implements AnalysisResult{

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
	public double purity;
	/**
	 *
	 */
	public double nmi;

	/**
	 * The total number of pairs in a cluster which belong to the same class
	 */
	public int TP;
	/**
	 * The total number of pairs in a cluster which do not belong to the same class
	 */
	public int FP;
	/**
	 * The total number of pairs in different clusters which belong to different classes
	 */
	public int TN;
	/**
	 * The total number of pairs in different clusters which belong to the same class
	 */
	public int FN;

	/**
	 * The proportion of correct decisions made against incorrect decisions (un-weighted)
	 */
	public double randIndex;

	/**
	 * The number of true positives as a proportion of overall positives
	 */
	public double precision;

	/**
	 * The number of true positives as a porportion of true positives and false negatives
	 */
	public double recall;


	@Override
	public String getSummaryReport() {
		return String.format("(purity=%2.5f,nmi=%2.5f,P=%2.2f,R=%2.5f,F1=%2.5f)",purity,nmi,precision,recall,fscore(1));
	}

	@Override
	public String getDetailReport() {
		return "";
	}

	/**
	 * @param beta
	 * @return the f-score which weights up or down the relative importance of a false positive against false negatives
	 */
	public double fscore(double beta){
		double beta2 = beta * beta;
		double P = this.precision;
		double R = this.recall;
		return ((beta2 + 1) * P * R)/(beta2 * P + R);
	}

	@Override
	public JasperPrint getSummaryReport(String title, String info) throws JRException {throw new UnsupportedOperationException();}

	@Override
	public JasperPrint getDetailReport(String title, String info)throws JRException {throw new UnsupportedOperationException();}

}