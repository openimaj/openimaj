package org.openimaj.ml.linear.learner.init;

import gov.sandia.cognition.math.matrix.Matrix;

/**
 * Initialise a matrix to some dimension
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface InitStrategy {

	/**
	 * @param rows
	 * @param cols
	 * @return a matrix of the requested dimensions
	 */
	public Matrix init(int rows, int cols);
}
