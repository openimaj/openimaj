package org.openimaj.ml.linear.learner.init;

import gov.sandia.cognition.math.matrix.Matrix;

public interface InitStrategy {
	public Matrix init(int rows, int cols);
}
