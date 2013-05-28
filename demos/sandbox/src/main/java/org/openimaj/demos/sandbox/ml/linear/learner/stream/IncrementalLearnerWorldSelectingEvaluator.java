package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.util.Map;

import org.openimaj.ml.linear.evaluation.BilinearEvaluator;
import org.openimaj.ml.linear.learner.IncrementalBilinearSparseOnlineLearner;
import org.openimaj.util.data.Context;
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
			Function<Context,Context>
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
	public Context apply(Context in)
	{
		ModelStats modelStats = null;
		if(learner!=null){
			modelStats = new ModelStats(eval, learner, in);
		}
		Map<String,Map<String,Double>> x = in.getTyped("bagofwords");
		Map<String,Double> y = in.getTyped("averageticks");
		learner = func.apply(IndependentPair.pair(x, y));
		if(modelStats == null) modelStats = new ModelStats();

		in.put("modelstats",modelStats);
		return in;
	}

}