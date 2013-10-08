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

import gnu.trove.list.array.TIntArrayList;

import java.util.Random;

import org.openimaj.experiment.evaluation.AnalysisResult;


/**
 * Wraps the functionality of any {@link ClusterAnalyser} as corrected by
 * a Random baseline. This implementation follows that of cluster eval:
 * http://chris.de-vries.id.au/2013/06/clustereval-10-release.html
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <ANNER>
 * @param <ANNYS>
 */
public class RandomBaselineClusterAnalyser<
		ANNER extends ClusterAnalyser<ANNYS>,
		ANNYS extends RandomBaselineWrappable & AnalysisResult> 
	implements 
		ClusterAnalyser<RandomBaselineClusterAnalysis<ANNYS>>
	{

	private static final int NUMBER_OF_TRIALS = 100;
	private ANNER ann;
	private int trials;
	private Random random;
	/**
	 * @param analyser the underlying analyser
	 * 
	 */
	public RandomBaselineClusterAnalyser(ANNER analyser) {
		this.ann = analyser;
		this.trials = NUMBER_OF_TRIALS;
		this.random = new Random();
	}
	
	/**
	 * @param analyser
	 * @param trials the number of random baselines to try, finding an average random score
	 */
	public RandomBaselineClusterAnalyser(ANNER analyser, int trials) {
		this.ann = analyser;
		this.trials = trials;
		this.random = new Random();
	}
	
	/**
	 * @param analyser
	 * @param trials the number of random baselines to try, finding an average random score
	 * @param seed 
	 */
	public RandomBaselineClusterAnalyser(ANNER analyser, int trials, long seed) {
		this.ann = analyser;
		this.trials = trials;
		this.random = new Random(seed);
	}
	@Override
	public RandomBaselineClusterAnalysis<ANNYS> analyse(int[][] correct,int[][] estimated) {
		ANNYS score = ann.analyse(correct, estimated);
		ANNYS randscore = ann.analyse(correct, baseline(estimated));
		double meanrand = randscore.score();
		for (int i = 0; i < this.trials; i++) {
			randscore = ann.analyse(correct, baseline(estimated));
			meanrand += randscore.score(); 
		}
		meanrand /= (trials+1);
		return new RandomBaselineClusterAnalysis<ANNYS>(score,meanrand );
	}
	private int[][] baseline(int[][] estimated) {
		TIntArrayList items = new TIntArrayList();
		for (int[] is : estimated) {
			for (int i = 0; i < is.length; i++) {
				items.add(is[i]);
			}
		}
		int[][] baseline = new int[estimated.length][];
		items.shuffle(this.random);
		int[] itemsArr = items.toArray();
		int seen = 0;
		for (int i = 0; i < baseline.length; i++) {
			int needed = estimated[i].length;
			baseline[i] = new int[needed];
			System.arraycopy(itemsArr, seen, baseline[i], 0, needed);
			seen+=needed;
		}
		
		return baseline;
	}

}
