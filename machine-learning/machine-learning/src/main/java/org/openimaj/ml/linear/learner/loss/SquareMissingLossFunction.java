package org.openimaj.ml.linear.learner.loss;

import gov.sandia.cognition.math.matrix.Matrix;

import org.apache.log4j.Logger;

public class SquareMissingLossFunction extends LossFunction {
	Logger logger = Logger.getLogger(SquareMissingLossFunction.class);

	@Override
	public Matrix gradient(Matrix W) {
		final Matrix resid = X.times(W).minus(Y);
		if (this.bias != null)
			resid.plusEquals(this.bias);
		for (int r = 0; r < Y.getNumRows(); r++) {
			final double yc = Y.getElement(r, 0);
			if (Double.isNaN(yc)) {
				resid.setElement(r, 0, 0);
			}
		}
		return X.transpose().times(resid);
	}

	@Override
	public double eval(Matrix W) {
		Matrix v;
		if (W == null) {
			v = this.X;
		}
		else {
			v = X.times(W);
		}
		final Matrix vWithoutBias = v.clone();
		if (this.bias != null)
			v.plusEquals(this.bias);
		double sum = 0;
		for (int r = 0; r < Y.getNumRows(); r++) {
			for (int c = 0; c < Y.getNumColumns(); c++) {
				final double yr = Y.getElement(r, c);
				if (!Double.isNaN(yr)) {
					final double val = v.getElement(r, c);
					final double valNoBias = vWithoutBias.getElement(r, c);
					final double delta = yr - val;
					logger.debug(
							String.format(
									"yr=%d,y=%3.2f,v=%3.2f,v(no bias)=%2.5f,delta=%2.5f",
									r, yr, val, valNoBias, delta
									)
							);
					sum += delta * delta;
				}
			}
		}
		return sum;
	}

}
