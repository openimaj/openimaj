package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.util.List;
import java.util.Map;

import org.openimaj.ml.linear.evaluation.BilinearEvaluator;
import org.openimaj.ml.linear.learner.IncrementalBilinearSparseOnlineLearner;
import org.openimaj.util.function.Function;
import org.openimaj.util.pair.IndependentPair;

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
				IndependentPair<Map<String, Double>, Map<String, Map<String, Double>>>,
				IndependentPair<List<String>, Double>
		>
{

	BilinearEvaluator eval;
	private IncrementalLearnerFunction func;
	private IncrementalBilinearSparseOnlineLearner oldLearner;


	public IncrementalLearnerWorldSelectingEvaluator(BilinearEvaluator eval, IncrementalLearnerFunction func) {
		this.eval = eval;
		this.func = func;
		this.oldLearner = null;
	}
	@Override
	public IndependentPair<List<String>, Double> apply(IndependentPair<Map<String, Double>, Map<String, Map<String, Double>>> in)
	{
//		if(oldLearner!=null){
//			this.oldLearner.asMatrixPair(xy, nfeatures, nusers, ntasks)
//		}
		return null;
	}
}