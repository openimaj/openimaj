package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import gov.sandia.cognition.math.matrix.Matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.math.matrix.SandiaMatrixUtils;
import org.openimaj.ml.linear.evaluation.BilinearEvaluator;
import org.openimaj.ml.linear.learner.BilinearSparseOnlineLearner;
import org.openimaj.ml.linear.learner.IncrementalBilinearSparseOnlineLearner;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;
import org.openimaj.util.stream.window.Aggregation;

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
	 * The value of Y for this round of the model
	 */
	public Matrix correctY;
	/**
	 * The estimated value of Y for this round of the model
	 */
	public Matrix estimatedY;

	/**
	 * A new learner, no meaningful important words and a loss of 0
	 */
	public ModelStats() {
		this.score = 0;
		this.learner = null;
		this.importantWords = new HashMap<String, SortedImportantWords>();
		this.taskMinMax = new HashMap<String, Pair<Double>>();
	}

	/**
	 * The model and its associated loss
	 * @param eval
	 * @param learner
	 * @param in
	 */
	public ModelStats(BilinearEvaluator eval, IncrementalBilinearSparseOnlineLearner learner, Aggregation<IndependentPair<Map<String, Map<String, Double>>, Map<String, Double>>, IndependentPair<Long, Long>> inaggr) {

		this.learner = learner;
		IndependentPair<Map<String, Map<String, Double>>, Map<String, Double>> in = inaggr.getPayload();
		this.learner.updateUserValues(in.firstObject(), in.secondObject());
		BilinearSparseOnlineLearner bilinearLearner = this.learner.getBilinearLearner();
		eval.setLearner(bilinearLearner);
		List<Pair<Matrix>> testList = new ArrayList<Pair<Matrix>>();
		Pair<Matrix> xy = this.learner.asMatrixPair(in);
		testList.add(xy);
		this.score = eval.evaluate(testList);
		this.importantWords = importantWords();
		this.taskMinMax = minMaxWords();
		this.correctY = xy.secondObject();
		this.estimatedY = bilinearLearner.predict(xy.firstObject());
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
