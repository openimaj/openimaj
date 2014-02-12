/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.experiment.evaluation.cluster.analyser;

import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.logger.LoggerUtils;

/**
 * The normalised mutual information of a cluster estimate
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class NMIClusterAnalyser implements ClusterAnalyser<NMIAnalysis> {

	private final static Logger logger = Logger.getLogger(NMIClusterAnalyser.class);

	@Override
	public NMIAnalysis analyse(int[][] correct, int[][] estimated) {
		final NMIAnalysis ret = new NMIAnalysis();
		final Map<Integer, Integer> invCor = ClusterAnalyserUtils.invert(correct);
		final Map<Integer, Integer> invEst = ClusterAnalyserUtils.invert(estimated);
		ret.nmi = nmi(correct, estimated, invCor, invEst);
		return ret;
	}

	private double nmi(int[][] c, int[][] e, Map<Integer, Integer> ic, Map<Integer, Integer> ie) {
		final double N = Math.max(ic.size(), ie.size());
		final double mi = mutualInformation(N, c, e, ic, ie);
		LoggerUtils.debugFormat(logger, "Iec = %2.5f", mi);
		final double ent_e = entropy(e, N);
		LoggerUtils.debugFormat(logger, "He = %2.5f", ent_e);
		final double ent_c = entropy(c, N);
		LoggerUtils.debugFormat(logger, "Hc = %2.5f", ent_c);
		return mi / ((ent_e + ent_c) / 2);
	}

	/**
	 * Maximum liklihood estimate of the entropy
	 * 
	 * @param clusters
	 * @param N
	 * @return
	 */
	private double entropy(int[][] clusters, double N) {
		double total = 0;
		for (int k = 0; k < clusters.length; k++) {
			LoggerUtils.debugFormat(logger, "%2.1f/%2.1f * log2 ((%2.1f / %2.1f) )", (double) clusters[k].length, N,
					(double) clusters[k].length, N);
			final double prop = clusters[k].length / N;
			total += prop * log2(prop);
		}
		return -total;
	}

	private double log2(double prop) {
		if (prop == 0)
			return 0;
		return Math.log(prop) / Math.log(2);
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
			final double n_e = e[k].length;
			for (int j = 0; j < c.length; j++) {
				final double n_c = c[j].length;
				double both = 0;
				for (int i = 0; i < e[k].length; i++) {
					final Integer itemCluster = ic.get(e[k][i]);
					if (itemCluster == null)
						continue;
					if (itemCluster == j)
						both++;
				}
				final double normProp = (both * N) / (n_c * n_e);
				// LoggerUtils.debugFormat(logger,"normprop = %2.5f",normProp);
				final double sum = (both / N) * (log2(normProp));
				mi += sum;

				// LoggerUtils.debugFormat(logger,"%2.1f/%2.1f * log2 ((%2.1f * %2.1f) / (%2.1f * %2.1f)) = %2.5f",both,N,both,N,n_c,n_e,sum);
			}
		}
		return mi;
	}

	// public static void main(String[] args) {
	// LoggerUtils.prepareConsoleLogger();
	// NMIClusterAnalyser an = new NMIClusterAnalyser();
	// NMIAnalysis res = an.analyse(
	// new int[][]{new int[]{1,2,3},new int[]{4,5,6}},
	// // new int[][]{new int[]{1,2},new int[]{3},new int[]{4,5},new int[]{6}}
	// // new int[][]{new int[]{1},new int[]{2},new int[]{3},new int[]{4},new
	// int[]{5},new int[]{6}}
	// new int[][]{new int[]{7,8,9}}
	// );
	// System.out.println(res);
	// }

}
