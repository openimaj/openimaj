package org.openimaj.ml.linear.learner.loss;

import org.apache.log4j.Logger;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;

public class SquareMissingLossFunction extends LossFunction{
	Logger logger = Logger.getLogger(SquareMissingLossFunction.class);
	@Override
	public Matrix gradient(Matrix W) {
		Matrix resid = X.times(W).minus(Y);
		if(this.bias!=null) 
			resid.plusEquals(this.bias);
		for (int r = 0; r < Y.getNumRows(); r++) {
			double yc = Y.getElement(r, 0);
			if(Double.isNaN(yc)){
				resid.setElement(r,0, 0);
			}
		}
		return X.transpose().times(resid);
	}

	@Override
	public double eval(Matrix W) {
		Matrix v ;
		if(W == null){
			v = this.X;
		}
		else{
			v = X.times(W);
		}
		Matrix vWithoutBias = v.clone();
		if(this.bias!=null) 
			v.plusEquals(this.bias);
		double sum = 0;
		for (int r = 0; r < Y.getNumRows(); r++) {
			for (int c = 0; c < Y.getNumColumns(); c++) {
				double yr = Y.getElement(r, c);
				if(!Double.isNaN(yr)){
					double val = v.getElement(r, c);
					double valNoBias = vWithoutBias.getElement(r, c);
					double delta = yr - val;
					logger.debug(
						String.format(
							"yr=%d,y=%3.2f,v=%3.2f,v(no bias)=%2.5f,delta=%2.5f",
							r,yr,val,valNoBias,delta
						)
					);
					sum += delta * delta ;
				}
			}
		}
		return sum;
	}
	
}
