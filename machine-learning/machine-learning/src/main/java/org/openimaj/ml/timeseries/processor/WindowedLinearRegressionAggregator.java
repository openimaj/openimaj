package org.openimaj.ml.timeseries.processor;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.ml.regression.LinearRegression;
import org.openimaj.ml.timeseries.SynchronisedTimeSeriesCollection;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * Using a {@link LinearRegression} model, a time series is used as input to calculate the coefficients 
 * of a linear regression such that value = b * time + c
 * 
 * This is the simplest kind of model that can be applied to a time series
 * @author ss
 *
 */
public class WindowedLinearRegressionAggregator implements SynchronisedTimeSeriesCollectionAggregator<
	DoubleTimeSeries, 
	SynchronisedTimeSeriesCollection<double[],Double,DoubleTimeSeries>, 
	DoubleTimeSeries>{

	private static final int DEFAULT_WINDOW_SIZE = 3;
	private static final int DEFAULT_OFFSET = 1;
	private LinearRegression reg;
	private boolean regdefined;
	private int windowsize;
	private int offset;
	private String ydataName;
	/**
	 * Calculate the regression from the same time series inputed
	 */
	public WindowedLinearRegressionAggregator() {
		this.windowsize = DEFAULT_WINDOW_SIZE;
		this.offset = DEFAULT_OFFSET;
		this.regdefined = false;
	}
	
	/**
	 * Perform regression s.t. y = Sum(w_{0-i} * x_{0-i}) + c for i from 1 to windowsize
	 * @param windowsize
	 */
	public WindowedLinearRegressionAggregator(int windowsize) {
		this.windowsize = windowsize;
		this.offset = DEFAULT_OFFSET;
		this.regdefined = false;
		
	}
	
	/**
	 * Perform regression s.t. y = Sum(w_{0-i} * x_{0-i}) + c for i from 1 to windowsize
	 * @param windowsize
	 * @param offset 
	 */
	public WindowedLinearRegressionAggregator(int windowsize, int offset) {
		this.windowsize = windowsize;
		this.offset = offset;
		this.regdefined = false;
		
	}
	
	/**
//	 * Use reg as the linear regression to predict. The {@link #process(DoubleTimeSeries)} function simply calls
//	 * {@link LinearRegression#predict(Matrix)} with the times in the series as input
//	 * @param reg
//	 */
//	public WindowedLinearRegressionAggregator(LinearRegression reg) {
//		this.reg = reg;
//		this.regdefined = true;
//	}
//
//	public WindowedLinearRegressionAggregator(DoubleTimeSeries yearFirstHalf,int i) {
//		WindowedLinearRegressionAggregator inner = new WindowedLinearRegressionAggregator(i);
//		inner.process(yearFirstHalf);
//		this.reg = inner.reg;
//		this.windowsize = i;
//		this.regdefined = true;
//	}
//	
//	public WindowedLinearRegressionAggregator(DoubleTimeSeries yearFirstHalf,int windowsize, int offset) {
//		WindowedLinearRegressionAggregator inner = new WindowedLinearRegressionAggregator(windowsize,offset);
//		inner.process(yearFirstHalf);
//		this.reg = inner.reg;
//		this.windowsize = windowsize;
//		this.offset = offset;
//		this.regdefined = true;
//	}

//	@Override
//	public void process(DoubleTimeSeries series) {
//		Matrix x = new Matrix(new double[][]{ArrayUtils.longToDouble(series.getTimes())}).transpose();
//		List<IndependentPair<double[], double[]>> instances = new ArrayList<IndependentPair<double[], double[]>>();
//		double[] data = series.getData();
//		
//		for (int i = this.windowsize + (offset - 1); i < series.size(); i++) {
//			int start = i - this.windowsize - (offset - 1);
////			System.out.format("Range %d->%d (inclusive) used to calculate: %d\n",start,start+this.windowsize-1,i);
//			double[] datawindow = new double[this.windowsize];
//			System.arraycopy(data, start, datawindow, 0, this.windowsize);
//			instances.add(IndependentPair.pair(datawindow, new double[]{data[i]}));
//		}
//		if(!regdefined)
//		{
//			this.reg = new LinearRegression();
//			this.reg.estimate(instances);
//		}
//		System.out.println(this.reg);
//		Iterator<IndependentPair<double[], double[]>> instanceIter = instances.iterator();
//		for (int i = this.windowsize + (offset - 1); i < series.size(); i++) {
//			data[i] = this.reg.predict(instanceIter.next().firstObject())[0];
//		}
//	}
	
	/**
	 * @param regdefined if true, process holds its last {@link LinearRegression}
	 */
	public void holdreg(boolean regdefined){
		this.regdefined = regdefined;
	}
	
	/**
	 * @return the {@link WindowedLinearRegressionAggregator}'s underlying {@link LinearRegression} model
	 */
	public LinearRegression getRegression() {
		return this.reg;
	}

	@Override
	public DoubleTimeSeries aggregate(SynchronisedTimeSeriesCollection<double[],Double,DoubleTimeSeries> series) {
		Matrix x = new Matrix(new double[][]{ArrayUtils.longToDouble(series.getTimes())}).transpose();
		List<IndependentPair<double[], double[]>> instances = new ArrayList<IndependentPair<double[], double[]>>();
		double[] data = series.flatten();
		double[] ydata = series.series(ydataName ).getData();
		int nseries = series.nSeries();
		for (int i = this.windowsize + (offset - 1); i < series.size(); i++) {
			int start = (i - this.windowsize - (offset - 1)) * nseries;
//			System.out.format("Range %d->%d (inclusive) used to calculate: %d\n",start,start+this.windowsize-1,i);
			double[] datawindow = new double[this.windowsize*nseries];
			System.arraycopy(data, start, datawindow, 0, this.windowsize*nseries);
			instances.add(IndependentPair.pair(datawindow, new double[]{ydata[i]}));
		}
		
		return null;
	}

}
