package org.openimaj.ml.regression;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openimaj.data.RandomData;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

public class LinearRegressionTest {
	@Test
	public void testLinearRegression() throws Exception {
		double[][] xt = new double[][]{new double[]{0,1,2,3,4,5,6,7,8,9}};
		// function params
		double m = 2;
		double c = -2;
		
		double[][] perfectyt = new double[1][xt[0].length];
		for (int i = 0; i < perfectyt[0].length; i++) {
			perfectyt[0][i] = xt[0][i] * m + c;
		}
		
		Matrix x = new Matrix(xt).transpose();
		Matrix perfecty = new Matrix(perfectyt).transpose();
		
		Matrix noise = new Matrix(RandomData.getRandomDoubleArray(perfecty.getRowDimension(), 1, -1, 1, 1));
		Matrix y = perfecty.plus(noise);
		
		List<IndependentPair<double[], double[]>> ipairs = aspairs(x,y);
		
		LinearRegression lr = new LinearRegression();
		lr.estimate(y, x);
		
		LinearRegression lr2 = new LinearRegression();
		lr2.estimate(ipairs);
		assertTrue(lr.equals(lr2));
		
		System.out.println("Error: " + lr.calculateError(ipairs));
		assertTrue(lr.calculateError(ipairs) < 5);
	}

	private List<IndependentPair<double[], double[]>> aspairs(Matrix x, Matrix y) {
		List<IndependentPair<double[], double[]>> ret = new ArrayList<IndependentPair<double[], double[]>>();
		double[][] xd = x.getArray();
		double[][] yd = y.getArray();
		for (int i = 0; i < xd.length; i++) {
			ret.add(IndependentPair.pair(xd[i], yd[i]));
		}
		return ret ;
	}
}
