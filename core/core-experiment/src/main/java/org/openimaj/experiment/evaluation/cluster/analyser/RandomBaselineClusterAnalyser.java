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

	private static final int NUMBER_OF_TRIALS = 10;
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
