package org.openimaj.ml.linear.learner.init;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;

/**
 * Completely ignores desired dimensions and returns what it wants
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class HardCodedInitStrat implements InitStrategy{

	private Matrix mat;

	@Override
	public Matrix init(int rows, int cols) {
		int mrows = mat.getNumRows();
		int mcols = mat.getNumColumns();
		if(mrows == rows && mcols == cols) return mat;
		
		if(rows == cols && mcols == rows){
			Matrix retMat = SparseMatrixFactoryMTJ.getSparseDefault().createMatrix(rows, rows);
			for (int i = 0; i < mcols; i++) {
				retMat.setElement(i, i, mat.getElement(0, i));
			}
			return retMat;
		}
		return mat;
	}
	
	public void setMatrix(Matrix m){
		this.mat = m;
	}

}
