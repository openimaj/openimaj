package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import gov.sandia.cognition.math.matrix.Matrix;

import org.openimaj.ml.linear.learner.BilinearSparseOnlineLearner;
import org.openimaj.util.function.Operation;
import org.openimaj.util.pair.Pair;

/**
 * A {@link Operation} backed by a {@link BilinearSparseOnlineLearner}
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class BilinearSparseOnlineLearnerOperator implements Operation<Pair<Matrix>>{

	private BilinearSparseOnlineLearner learner;
	/**
	 * Provide the backing learner
	 * @param learner
	 */
	public BilinearSparseOnlineLearnerOperator(BilinearSparseOnlineLearner learner) {
		this.learner = learner;
	}

	/**
	 * Creates a default learner
	 */
	public BilinearSparseOnlineLearnerOperator() {
		this.learner = new BilinearSparseOnlineLearner();
	}

	@Override
	public void perform(Pair<Matrix> object) {
		learner.process(object.firstObject(), object.secondObject());
	}

}
