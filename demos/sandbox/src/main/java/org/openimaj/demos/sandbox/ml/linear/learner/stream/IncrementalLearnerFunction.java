package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.util.Map;

import org.openimaj.ml.linear.learner.BilinearLearnerParameters;
import org.openimaj.ml.linear.learner.IncrementalBilinearSparseOnlineLearner;
import org.openimaj.util.function.Function;
import org.openimaj.util.pair.IndependentPair;

/**
 * Consumes Y and X instances for an {@link IncrementalBilinearSparseOnlineLearner} which are used
 * to measure loss of an underlying model, and then used to train the underlying model.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class IncrementalLearnerFunction implements Function<
		IndependentPair<
			Map<String,Map<String,Double>>,	
			Map<String,Double>
		>,
		IncrementalBilinearSparseOnlineLearner> {

	final IncrementalBilinearSparseOnlineLearner learner;

	/**
	 * Constructs the underlying learner to train
	 */
	public IncrementalLearnerFunction() {
		this.learner = new IncrementalBilinearSparseOnlineLearner();
	}

	/**
	 * Feeds the parameters to a new learner to train
	 * @param params
	 */
	public IncrementalLearnerFunction(BilinearLearnerParameters params) {
		this.learner = new IncrementalBilinearSparseOnlineLearner(params);
	}
	/**
	 * Takes an existing learner and continues training it.
	 * @param learner
	 */
	public IncrementalLearnerFunction(IncrementalBilinearSparseOnlineLearner learner) {
		this.learner = learner;
	}



	@Override
	public IncrementalBilinearSparseOnlineLearner apply(IndependentPair<Map<String, Map<String, Double>>,Map<String, Double>> in)
	{
		learner.process(in.getFirstObject(),in.getSecondObject());
		System.out.printf("Learner has learnt %d words\n",learner.getVocabulary().size());
		return this.learner;
	}

}

