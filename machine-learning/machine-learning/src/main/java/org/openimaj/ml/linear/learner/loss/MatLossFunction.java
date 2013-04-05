package org.openimaj.ml.linear.learner.loss;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;

public class MatLossFunction extends LossFunction{
	
	private LossFunction f;
	private SparseMatrixFactoryMTJ spf;
	public MatLossFunction(LossFunction f) {
		this.f = f;
		spf = SparseMatrixFactoryMTJ.INSTANCE;
	}
	
	@Override
	public void setX(Matrix X) {
		super.setX(X);
		f.setX(X);
	}
	
	@Override
	public void setY(Matrix Y) {
		super.setY(Y);
		f.setY(Y);
	}
	
	@Override
	public void setBias(Matrix bias) {
		super.setBias(bias);
		f.setBias(bias);
	}
	@Override
	public Matrix gradient(Matrix W) {
		SparseMatrix ret = spf.createMatrix(W.getNumRows(), W.getNumColumns());
		int allRowsY = Y.getNumRows()-1;
		int allRowsW = W.getNumRows()-1;
		for (int i = 0; i < Y.getNumColumns(); i++) {
			this.f.setY(Y.getSubMatrix(0, allRowsY, i, i));
			if(bias!=null) this.f.setBias(bias.getSubMatrix(0, allRowsY, i, i));
			Matrix submatrix = f.gradient(W.getSubMatrix(0, allRowsW, i, i));
			ret.setSubMatrix(0, i, submatrix);
		}
		return ret;
	}

	@Override
	public double eval(Matrix W) {
		double total = 0;
		f.setBias(this.bias);
		total += f.eval(W);
		return total;
	}

}
