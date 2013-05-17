package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.util.HashMap;
import java.util.Map;

import org.openimaj.math.matrix.SandiaMatrixUtils;
import org.openimaj.ml.linear.learner.BilinearSparseOnlineLearner;
import org.openimaj.ml.linear.learner.IncrementalBilinearSparseOnlineLearner;
import org.openimaj.util.pair.Pair;

import com.google.common.collect.BiMap;

/**
 * Some stats of a model
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ModelStats {

	/**
	 * The score of the model estimating the the data before this round
	 */
	public double score;
	/**
	 * The sorted important words of the model after this round
	 */
	public Map<String, SortedImportantWords> importantWords;
	/**
	 * The current model
	 */
	public IncrementalBilinearSparseOnlineLearner learner;

	/**
	 * the min/max params of each task
	 */
	public Map<String,Pair<Double>> taskMinMax;

	/**
	 * The model and its associated loss
	 * @param learner
	 * @param score
	 */
	public ModelStats(IncrementalBilinearSparseOnlineLearner learner, double score) {
		this.score = score;
		this.learner = learner;
		this.importantWords = importantWords();
		this.taskMinMax = minMaxWords();
	}

	private Map<String, Pair<Double>> minMaxWords() {
		Map<String, Pair<Double>> ret = new HashMap<String, Pair<Double>>();
		if(this.learner == null) return ret;
		BiMap<String, Integer> depvals = this.learner.getDependantValues();
		BilinearSparseOnlineLearner bilearner = this.learner.getBilinearLearner();
		for (String task : depvals.keySet()) {
			ret.put(
				task,
				new Pair<Double>(
					SandiaMatrixUtils.min(bilearner.getW()),
					SandiaMatrixUtils.max(bilearner.getW())
				)
			);
		}

		return ret;
	}

	private Map<String, SortedImportantWords> importantWords() {
		Map<String, SortedImportantWords> ret = new HashMap<String, SortedImportantWords>();
		if(this.learner == null) return ret;
		BiMap<String, Integer> depvals = this.learner.getDependantValues();
		BilinearSparseOnlineLearner bilearner = this.learner.getBilinearLearner();
		for (String task : depvals.keySet()) {
			ret.put(
				task,
				new SortedImportantWords(task, learner, bilearner, 10)
			);
		}

		return ret;
	}

}
