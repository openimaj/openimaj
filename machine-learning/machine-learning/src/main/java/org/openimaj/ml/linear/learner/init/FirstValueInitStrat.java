package org.openimaj.ml.linear.learner.init;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;

/**
 * Completely ignores desired dimensions and returns the first
 * Y value seen
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class FirstValueInitStrat extends AbstractContextAwareInitStrategy<Matrix,Matrix>{

	@Override
	public Matrix init(int rows, int cols) {
		int mrows = y.getNumRows();
		int mcols = y.getNumColumns();
		if(mrows == rows && mcols == cols) return y;

		if(rows == cols && mcols == rows){
			Matrix retMat = SparseMatrixFactoryMTJ.getSparseDefault().createMatrix(rows, rows);
			for (int i = 0; i < mcols; i++) {
				retMat.setElement(i, i, y.getElement(0, i));
			}
			return retMat;
		}
		return y;
	}
}
