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

import org.openimaj.experiment.evaluation.AnalysisResult;

import gov.sandia.cognition.math.MathUtil;

/**
 * Gather the true positives, true negatives, false positives, false negatives for a {@link DecisionAnalysis} instance
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T> the type of analysis
 */
public abstract class DecisionClusterAnalyser<T extends AnalysisResult> implements ClusterAnalyser<T> {

	@Override
	public T analyse(int[][] correct, int[][] estimated) {
		DecisionAnalysis ret = new DecisionAnalysis();
		Map<Integer,Integer> invCor = ClusterAnalyserUtils.invert(correct);
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
				Integer integer = invCor.get(i);
				if(integer  == null) continue;
				classCount[integer]++;
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
		
		return analysisResults(ret);
	}

	/**
	 * Given a {@link DecisionAnalysis}, construct an analysis
	 * @param ret
	 * @return an analysis
	 */
	public abstract T analysisResults(DecisionAnalysis ret) ;

}
