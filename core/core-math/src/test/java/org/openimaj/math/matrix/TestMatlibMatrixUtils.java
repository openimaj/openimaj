package org.openimaj.math.matrix;

import static org.junit.Assert.*;
import no.uib.cipr.matrix.Matrix.Norm;

import org.junit.Test;

import ch.akuhn.matrix.DenseMatrix;
import ch.akuhn.matrix.Matrix;


public class TestMatlibMatrixUtils {
	@Test
	public void testDotProduct() throws Exception {
		Matrix X = new DenseMatrix( new double[][] {
				{ 1, 1, },
				{ 2, 2, },
		});
		Matrix expected = new DenseMatrix( new double[][] {
				{ 3, 3, },
				{ 6, 6, },
		});
		Matrix m = MatlibMatrixUtils.dotProduct(X, X);
		double[][] asArray = MatlibMatrixUtils.minusInplace(m, expected).asArray();
		assertTrue(new no.uib.cipr.matrix.DenseMatrix(asArray).norm(Norm.Frobenius) == 0);
	}
}
