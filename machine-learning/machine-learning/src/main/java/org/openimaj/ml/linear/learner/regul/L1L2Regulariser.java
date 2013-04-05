package org.openimaj.ml.linear.learner.regul;

import org.apache.log4j.Logger;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.Vector;

public class L1L2Regulariser implements Regulariser{

	private static final Logger logger = Logger.getLogger(L1L2Regulariser.class);

	@Override
	public Matrix prox(Matrix W, double lambda) {
		int nrows = W.getNumRows();
		Matrix ret = W.clone();
		ret.zero();
		
		for (int r = 0; r < nrows; r++) {
			Vector row = W.getRow(r);
			double rownorm = row.norm2();
			if(rownorm > lambda){
				double scal = (rownorm - lambda)/rownorm;
				ret.setRow(r,row.scale(scal));
//				Vector setrow = ret.getRow(r);
//				logger .debug(setrow);
			}
		}
		return ret;
	}

}
