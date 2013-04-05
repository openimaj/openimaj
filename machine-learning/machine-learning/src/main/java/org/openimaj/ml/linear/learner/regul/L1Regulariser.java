package org.openimaj.ml.linear.learner.regul;

import gov.sandia.cognition.math.matrix.Matrix;

public class L1Regulariser implements Regulariser{

	@Override
	public Matrix prox(Matrix W, double lambda) {
		return softThreshold(W,lambda);
	}

	private Matrix softThreshold(Matrix w, double lambda) {
		Matrix ret = w.clone();
		ret.zero();
		int nrow = w.getNumRows();
		int ncol = w.getNumColumns();
		for (int r = 0; r < nrow; r++) {
			for (int c = 0; c < ncol; c++) {
				double v = ret.getElement(r, c);
				if(v < -lambda){
					ret.setElement(r, c, v + lambda);
				}
				else if(v > lambda){
					ret.setElement(r, c, v - lambda);
				}
			}
		}
		return ret;
	}
	
	

}
