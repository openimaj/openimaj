package org.openimaj.experiment.evaluation.cluster.analyser;

import gnu.trove.map.hash.TIntIntHashMap;
import gov.sandia.cognition.math.MathUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openimaj.logger.LoggerUtils;
import org.openimaj.util.pair.IntIntPair;


/**
 * A set of measures used to evaulate clustering.
 * These metrics are taken from: http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class MEClusterAnalyser implements ClusterAnalyser<MEAnalysis>{
	private Logger logger = Logger.getLogger(MEClusterAnalyser.class);

	@Override
	public MEAnalysis analyse(int[][] correct, int[][] estimated) {
		Map<Integer,Integer> invCor = invert(correct);
		Map<Integer,Integer> invEst = invert(estimated);
		MEAnalysis ret = new MEAnalysis();
		ret.purity = purity(correct,estimated,invCor,invEst);
//		ret.nmi = nmi(correct,estimated,invCor,invEst);
		decisions(ret,correct,estimated,invCor,invEst);
		adjustedRI(ret,correct,estimated);
		return ret;
	}

	private void adjustedRI(MEAnalysis ret, int[][] correct, int[][] estimated) {
		long[][] nij = new long[correct.length][estimated.length];
		long[] ni = new long[correct.length];
		long[] nj = new long[estimated.length];
		
		Map<Integer,Set<Integer>> estimatedSets = new HashMap<Integer, Set<Integer>>();
		int i = 0;
		for (int[] js : estimated) {
			Set<Integer> set = fillSet(js);
			estimatedSets.put(i++, set);
		}
		
		i=0;
		for (int[] is : correct) {
			int j = 0;
			Set<Integer> cset = fillSet(is);
			for (Entry<Integer, Set<Integer>> ks : estimatedSets.entrySet()) {
				HashSet<Integer> tmp = new HashSet<Integer>();
				tmp.addAll(cset);
				tmp.retainAll(ks.getValue());
				int count = tmp.size();
				nij[i][j] += count;
				ni[i] += count;
				nj[j] += count;
				j++;
			}
			i++;
		}
		
		long sumnij = 0;
		long sumni  = 0;
		long sumnj  = 0;
		long sumn   = 0;
		
		for (i = 0; i < nij.length; i++) {
			sumnj = 0;
			for (int j = 0; j < nij[i].length; j++) {
				if(nij[i][j] > 1){
					sumnij += MathUtil.binomialCoefficient((int) nij[i][j], 2);
				}
				if(nj[j] > 1){
					sumnj += MathUtil.binomialCoefficient((int) nj[j], 2);
				}
			}
			if(ni[i] > 1){
				sumni += MathUtil.binomialCoefficient((int) ni[i], 2);
			}
			sumn += ni[i];
		}
		double bisumn = MathUtil.binomialCoefficient((int) sumn, 2);
		double div = (sumni * sumnj) / bisumn;
		ret.adjRandInd = (sumnij - div) / (0.5 * (sumni + sumnj) - div); 
	}

	private Set<Integer> fillSet(int[] js) {
		HashSet<Integer> set = new HashSet<Integer>();
		for (Integer integer : js) set.add(integer);
		return set;
	}

	private void decisions(MEAnalysis ret, int[][] correct, int[][] estimated, Map<Integer, Integer> invCor,Map<Integer, Integer> invEst)
	{
		long positive = 0;
		long negative = 0;
		long remainingTotal = 0;
		long[] remaining = new long[correct.length];
		long[] classCount = new long[correct.length];
		// Count the remaining items not yet clustered, and the remaining items in each class not yet clustered
		for (int i = 0; i < correct.length; i++) {
			remainingTotal += correct[i].length;
			remaining[i] = correct[i].length;
		}
		ret.TP = ret.FN = 0;
		// Go through each estimated class
		for (int[] cluster : estimated) {
			// The potential correct pairings is calculated as a cluster length pick 2
			if(cluster.length > 1)
				positive += MathUtil.binomialCoefficient(cluster.length, 2);
			remainingTotal -= cluster.length;
			// The potential negative pairings is the size of this class times the remaining items
			negative += remainingTotal  * cluster.length;

			// We count the number of each class contained in this cluster
			for (int i : cluster) {
				classCount[invCor.get(i)]++;
			}
			// For each class, if its count is more than one update the true positives.
			// calculate the false negative pairings by considering the number for this class NOT in this cluster
			for (int i = 0; i < classCount.length; i++) {
				if(classCount[i] > 1){
					ret.TP += MathUtil.binomialCoefficient((int)classCount[i], 2);
				}
				remaining[i] -= classCount[i];
				ret.FN += remaining[i] * classCount[i];
				classCount[i] = 0; // reset the class count
			}
		}
		ret.FP = positive - ret.TP;
		ret.TN = negative - ret.FN;
		
		if( ret.TP + ret.FP == 0){
			ret.precision = 0;
		}
		ret.precision = ret.TP / (double)(ret.TP + ret.FP);
		ret.recall = ret.TP / (double)(ret.TP + ret.FN);
	}

	private double nmi(int[][] c, int[][] e, Map<Integer, Integer> ic, Map<Integer, Integer> ie) {
		double N = Math.max(ic.size(), ie.size());
		double mi = mutualInformation(N, c,e,ic,ie);
		LoggerUtils.debugFormat(logger,"Iec = %2.5f",mi);
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

	/**
	 * Calculate purity. The *class* of a given point is its cluster in the ground truth.
	 *
	 * @param correct
	 * @param estimated
	 * @param invCor
	 * @param invEst
	 * @return
	 */
	private double purity(int[][] correct, int[][] estimated, Map<Integer, Integer> invCor, Map<Integer, Integer> invEst) {
		double sumPurity = 0;
		for (int k = 0; k < estimated.length; k++) {
			IntIntPair maxClassCount = findMaxClassCount(estimated[k],invCor);
			sumPurity+=maxClassCount.second;
		}
		return sumPurity/invEst.size();
	}

	private IntIntPair findMaxClassCount(int[] is, Map<Integer, Integer> invCor) {
		TIntIntHashMap classCounts = new TIntIntHashMap();
		int max = 0;
		int c = 0;
		for (int i : is) {
			Integer c_i = invCor.get(i);
			int count = classCounts.adjustOrPutValue(c_i, 1, 1);
			if(count > max){
				max = count;
				c = c_i;
			}
		}
		return IntIntPair.pair(c, max);
	}

	private Map<Integer, Integer> invert(int[][] clustered) {
		int cluster = 0;
		Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
		for (int[] is : clustered) {
			for (int index : is) {
				ret.put(index, cluster);
			}
			cluster++;
		}
		return ret;
	}
}
