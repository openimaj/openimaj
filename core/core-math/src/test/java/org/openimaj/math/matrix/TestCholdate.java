package org.openimaj.math.matrix;


import static org.junit.Assert.*;

import org.junit.Test;
import org.openimaj.data.RandomData;

import ch.akuhn.matrix.DenseVector;
import Jama.Matrix;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class TestCholdate {
	
	@Test
	public void testRandomUpdate(){
		Matrix X = Matrix.random(100, 10);
		Matrix V = X.transpose().times(X);
		UpdateableCholeskyDecomposition c = new UpdateableCholeskyDecomposition(V);
		
		Matrix R = c.getL();
		
		double[] udata = RandomData.getRandomDoubleArray(R.getRowDimension(), 0, 1);
		Matrix u = MatlibMatrixUtils.toColJama(DenseVector.wrap(udata));
		Matrix V1 = V.plus(u.times(u.transpose()));
		
		Matrix R1 = new UpdateableCholeskyDecomposition(V1).getL();
		c.cholupdate(udata);
		Matrix R1_ = c.getL();
		assertTrue(MatrixUtils.equals(R1,R1_,0.00001));
	}
	
	@Test
	public void testRandomDowndate(){
		Matrix X = Matrix.random(100, 10);
		Matrix V = X.transpose().times(X);
		UpdateableCholeskyDecomposition c = new UpdateableCholeskyDecomposition(V);
		
		Matrix R = c.getL().copy();
		
		double[] udata = RandomData.getRandomDoubleArray(R.getRowDimension(), 0, 1);
		Matrix u = MatlibMatrixUtils.toColJama(DenseVector.wrap(udata));
		
		c.cholupdate(udata);
		c.choldowndate(udata);
		Matrix R1_ = c.getL();
		assertTrue(MatrixUtils.equals(R,R1_,0.00001));
	}

}
