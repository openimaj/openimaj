package org.openimaj.ml.linear.learner.regul;

import gov.sandia.cognition.math.matrix.Matrix;

public interface Regulariser {
	public Matrix prox(Matrix W, double lambda);
}
