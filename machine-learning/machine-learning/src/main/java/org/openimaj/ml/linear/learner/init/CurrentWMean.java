package org.openimaj.ml.linear.learner.init;

import gov.sandia.cognition.math.matrix.Matrix;

import org.openimaj.ml.linear.learner.BilinearSparseOnlineLearner;

/**
 * Useable only with {@link BilinearSparseOnlineLearner} instances. Uses the {@link BilinearSparseOnlineLearner#getW()}
 * for the current value
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class CurrentWMean extends CurrentValueMean{

	@Override
	public Matrix getCurrentValues() {
		return ((BilinearSparseOnlineLearner)this.learner).getW();
	}

}
