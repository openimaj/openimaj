package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import gov.sandia.cognition.math.matrix.Matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.ml.linear.evaluation.BilinearEvaluator;
import org.openimaj.ml.linear.learner.BilinearSparseOnlineLearner;
import org.openimaj.ml.linear.learner.IncrementalBilinearSparseOnlineLearner;
import org.openimaj.util.function.Function;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;

import com.google.common.collect.BiMap;

/**
 * Given a new state from which to train an {@link IncrementalBilinearSparseOnlineLearner},
 * This function:
 * 	- evaluates an old learner on the new state,
 * 	- selects important words from the new learner
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public final class IncrementalLearnerWorldSelectingEvaluator
		implements
			Function<
				IndependentPair<Map<String, Map<String, Double>>,Map<String, Double>>,
				IndependentPair<Map<String, SortedImportantWords>, Double>
		>
{

	BilinearEvaluator eval;
	private IncrementalLearnerFunction func;
	private IncrementalBilinearSparseOnlineLearner learner ;


	/**
	 * The evaluation to apply before learning, the function to feed examples for learning
	 * @param eval
	 * @param func
	 */
	public IncrementalLearnerWorldSelectingEvaluator(BilinearEvaluator eval, IncrementalLearnerFunction func) {
		this.eval = eval;
		this.func = func;
		this.learner = null;
	}
	@Override
	public IndependentPair<Map<String, SortedImportantWords>, Double> apply(IndependentPair<Map<String, Map<String, Double>>,Map<String, Double>> in)
	{
		double score = 0;
		if(learner!=null){
			learner.updateUserValues(in.firstObject(), in.secondObject());
			eval.setLearner(this.learner.getBilinearLearner());
			List<Pair<Matrix>> testList = new ArrayList<Pair<Matrix>>();
			testList.add(this.learner.asMatrixPair(in));
			score = eval.evaluate(testList);
		}
		learner = func.apply(in);

		return IndependentPair.pair(importantWords(), score);
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