package org.openimaj.experiment.evaluation.cluster.analyser;

import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.procedure.TIntLongProcedure;
import gov.sandia.cognition.math.MathUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Create an {@link AdjustedRandomIndexAnalysis} instance 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class AdjustedRandomIndexClusterAnalyser implements ClusterAnalyser<AdjustedRandomIndexAnalysis>{
	final static Logger logger = Logger.getLogger(AdjustedRandomIndexAnalysis.class);
	@Override
	public AdjustedRandomIndexAnalysis analyse(int[][] correct, int[][] estimated) {
		TIntLongHashMap nij = new TIntLongHashMap();
		TIntLongHashMap ni = new TIntLongHashMap();
		TIntLongHashMap nj = new TIntLongHashMap();
		
		Map<Integer,Integer> invCor = ClusterAnalyserUtils.invert(correct);
		logger.debug("Correct keys: " + invCor.size());
		Map<Integer,Integer> invEst = ClusterAnalyserUtils.invert(estimated);
		logger.debug("Estimated keys: " + invCor.size());
		Set<Integer> sharedKeys = new HashSet<Integer>();
		sharedKeys.addAll(invCor.keySet());
		sharedKeys.retainAll(invEst.keySet());
		logger.debug("Shared keys: " + sharedKeys.size());
		for (Integer index : sharedKeys) {
			int i = invCor.get(index);
			int j = invEst.get(index);
			nij.adjustOrPutValue(i * correct.length + j, 1, 1);
			ni.adjustOrPutValue(i, 1, 1);
			nj.adjustOrPutValue(j, 1, 1);
		}
		
		final long[] sumnij = new  long[1];
		final long[] sumni  = new long[1];
		final long[] sumnj  = new long[1];
		final long[] sumn   = new long[1];
		
		nj.forEachEntry(new TIntLongProcedure() {
			
			@Override
			public boolean execute(int a, long b) {
				if(b > 1){
					sumnj[0] += MathUtil.binomialCoefficient((int)b, 2);
				}
				return true;
			}
		});
		
		ni.forEachEntry(new TIntLongProcedure() {
			
			@Override
			public boolean execute(int a, long b) {
				if(b > 1){
					sumni[0] += MathUtil.binomialCoefficient((int)b, 2);
				}
				sumn[0] += b;
				return true;
			}
		});
		nij.forEachEntry(new TIntLongProcedure() {
			
			@Override
			public boolean execute(int a, long b) {
				if(b > 1){
					sumnij[0] += MathUtil.binomialCoefficient((int)b, 2);
				}
				return true;
			}
		});
		double bisumn = MathUtil.binomialCoefficient((int) sumn[0], 2);
		double div = (sumni[0] * sumnj[0]) / bisumn;
		AdjustedRandomIndexAnalysis ret = new AdjustedRandomIndexAnalysis();
		ret.adjRandInd = (sumnij[0] - div) / (0.5 * (sumni[0] + sumnj[0]) - div);
		
		return ret;
	}

}
