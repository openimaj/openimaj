package org.openimaj.ml.linear.learner.init;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.mtj.DenseMatrixFactoryMTJ;

/**
 * Given a matrix considered its "current value" this init strategy takes the current value
 * and averages the columns (creating the mean row). This row is used as the value for
 * all the rows in the initialise matrix
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class CurrentValueMean extends AbstractContextAwareInitStrategy<Matrix, Matrix>{

	@Override
	public Matrix init(int rows, int cols) {
		Matrix currentValues = getCurrentValues();
		Vector mean = currentValues.sumOfRows().scale(1f/currentValues.getNumRows());
		Matrix m = DenseMatrixFactoryMTJ.INSTANCE.createMatrix(rows, cols);
		for (int r = 0; r < m.getNumRows(); r++) {
			m.setRow(r, mean);
		}

		return m;
	}

	/**
	 * @return the matrix treated as the current value
	 */
	public abstract Matrix getCurrentValues() ;

}
