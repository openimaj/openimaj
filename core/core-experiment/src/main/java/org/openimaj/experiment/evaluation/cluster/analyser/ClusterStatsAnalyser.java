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

import org.openimaj.experiment.evaluation.cluster.analyser.ClusterStatsAnalysis.ClusterStats;

/**
 * Analysers extracts stats of the correct and estimated clusterings
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class ClusterStatsAnalyser implements ClusterAnalyser<ClusterStatsAnalysis>{

	@Override
	public ClusterStatsAnalysis analyse(int[][] correct, int[][] estimated) {
		ClusterStatsAnalysis ret = new ClusterStatsAnalysis();
		ret.correct = stats(correct);
		ret.estimated = stats(estimated);
		return ret;
	}

	private ClusterStats stats(int[][] correct) {
		ClusterStats ret = new ClusterStats();
		ret.max = 0;
		ret.min = Integer.MAX_VALUE;
		ret.mean = 0;
		
		for (int[] is : correct) {
			ret.max = Math.max(ret.max, is.length);
			ret.min = Math.min(ret.min, is.length);
			ret.mean += is.length / (double)correct.length;
			ret.total += is.length;
			ret.nc ++;
		}
		return ret;
	}

}
