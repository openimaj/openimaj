package org.openimaj.experiment.evaluation.cluster.analyser;

import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.logger.LoggerUtils;

/**
 * The normalised mutual information of a cluster estimate
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class NMIClusterAnalyser implements ClusterAnalyser<NMIAnalysis>{

	private final static Logger logger = Logger.getLogger(NMIClusterAnalyser.class);

	@Override
	public NMIAnalysis analyse(int[][] correct, int[][] estimated) {
		NMIAnalysis ret = new NMIAnalysis();
		Map<Integer,Integer> invCor = ClusterAnalyserUtils.invert(correct);
		Map<Integer,Integer> invEst = ClusterAnalyserUtils.invert(estimated);
		ret.nmi = nmi(correct,estimated,invCor,invEst);
		return ret;
	}
	private double nmi(int[][] c, int[][] e, Map<Integer, Integer> ic, Map<Integer, Integer> ie) {
		double N = Math.max(ic.size(), ie.size());
		double mi = mutualInformation(N, c,e,ic,ie);
		LoggerUtils.debugFormat(logger ,"Iec = %2.5f",mi);
		double ent_e = entropy(e,N);
		LoggerUtils.debugFormat(logger,"He = %2.5f",ent_e);
		double ent_c = entropy(c,N);
		LoggerUtils.debugFormat(logger,"Hc = %2.5f",ent_c);
		return mi / ((ent_e + ent_c)/2);
	}

	/**
	 * Maximum liklihood estimate of the entropy
	 * @param clusters
	 * @param N
	 * @return
	 */
	private double entropy(int[][] clusters, double N) {
		double total = 0;
		for (int k = 0; k < clusters.length; k++) {
			LoggerUtils.debugFormat(logger, "%2.1f/%2.1f * log2 ((%2.1f / %2.1f) )",(double)clusters[k].length,N,(double)clusters[k].length,N);
			double prop = clusters[k].length / N;
			total += prop * log2(prop);
		}
		return -total;
	}

	private double log2(double prop) {
		if(prop == 0) return 0;
		return Math.log(prop)/Math.log(2);
	}

	/**
	 * Maximum Liklihood estimate of the mutual information
	 *
	 * @param c
	 * @param e
	 * @param ic
	 * @param ie
	 * @return
	 */
	private double mutualInformation(double N, int[][] c, int[][] e, Map<Integer, Integer> ic, Map<Integer, Integer> ie) {
		double mi = 0;
		for (int k = 0; k < e.length; k++) {
			double n_e = e[k].length;
			for (int j = 0; j < c.length; j++) {
				double n_c = c[j].length;
				double both = 0;
				for (int i = 0; i < e[k].length; i++) {
					if(ic.get(e[k][i]) == j) both++;
				}
				double normProp = (both * N)/(n_c * n_e);
//				LoggerUtils.debugFormat(logger,"normprop = %2.5f",normProp);
				double sum = (both / N) * (log2(normProp));
				mi += sum;

//				LoggerUtils.debugFormat(logger,"%2.1f/%2.1f * log2 ((%2.1f * %2.1f) / (%2.1f * %2.1f)) = %2.5f",both,N,both,N,n_c,n_e,sum);
			}
		}
		return mi;
	}

}
