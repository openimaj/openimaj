package org.openimaj.ml.regression;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openimaj.data.RandomData;
import org.openimaj.ml.timeseries.aggregator.WindowedLinearRegressionAggregator;
import org.openimaj.ml.timeseries.series.DoubleSynchronisedTimeSeriesCollection;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

public class WindowedLinearRegressionTest {
	@Test
	public void testLinearRegression() throws Exception {
		long[] time = new long[]					{1,2,3,4,5 ,6 ,7 ,8 ,9 ,10,11};
		double[][] xt1 = new double[][]{new double[]{0,1,2,3,4 ,5 ,6 ,7 ,8 ,9 ,10}};
		double[][] xt2 = new double[][]{new double[]{0,1,4,9,16,25,36,49,64,81,100}};
		// function params
		double[] m = new double[]{2,10};
		double c = -2;
		int windowSize = 2;
		int offset = 1;
		
		double[][] perfectyt = new double[1][xt1[0].length];
		for (int i = windowSize; i < perfectyt[0].length; i++) {
			perfectyt[0][i] = c;
			for(int j = 0; j < windowSize; j++){
				int index = i - (windowSize - (offset-1)) + j;
				perfectyt[0][i] += xt1[0][index] * m[0] + xt2[0][index]*m[1] ;
			}
		}
		
		Matrix perfecty = new Matrix(perfectyt).transpose();
		
		Matrix noise = new Matrix(RandomData.getRandomDoubleArray(perfecty.getRowDimension(), 1, -1, 1, 2));
		Matrix y = perfecty.plus(noise).transpose();
		
		DoubleSynchronisedTimeSeriesCollection dstsc = new DoubleSynchronisedTimeSeriesCollection();
		dstsc.addTimeSeries("y", new DoubleTimeSeries(time, y.getArray()[0]));
		dstsc.addTimeSeries("xt1", new DoubleTimeSeries(time, xt1[0]));
		dstsc.addTimeSeries("xt2", new DoubleTimeSeries(time, xt2[0]));
		
		WindowedLinearRegressionAggregator wlra = new WindowedLinearRegressionAggregator("y",windowSize,offset,false);
		
		wlra.aggregate(dstsc);
		System.out.println(wlra.getReg());
		
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
