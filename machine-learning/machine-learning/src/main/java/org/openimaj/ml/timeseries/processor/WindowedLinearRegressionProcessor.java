package org.openimaj.ml.timeseries.processor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.ml.regression.LinearRegression;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * An implementation of an autoregressive model such that
 * Xt = b*X{t-offset-window,t-offset} + c
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class WindowedLinearRegressionProcessor implements TimeSeriesProcessor<double[], Double, DoubleTimeSeries>{

	private static final int DEFAULT_WINDOW_SIZE = 3;
	private static final int DEFAULT_OFFSET = 1;
	private LinearRegression reg;
	private boolean regdefined;
	private int windowsize;
	private int offset;
	/**
	 * Calculate the regression from the same time series inputed
	 */
	public WindowedLinearRegressionProcessor() {
		this.windowsize = DEFAULT_WINDOW_SIZE;
		this.offset = DEFAULT_OFFSET;
		this.regdefined = false;
	}
	
	/**
	 * Perform regression s.t. y = Sum(w_{0-i} * x_{0-i}) + c for i from 1 to windowsize
	 * @param windowsize
	 */
	public WindowedLinearRegressionProcessor(int windowsize) {
		this.windowsize = windowsize;
		this.offset = DEFAULT_OFFSET;
		this.regdefined = false;
		
	}
	
	/**
	 * Perform regression s.t. y = Sum(w_{0-i} * x_{0-i}) + c for i from 1 to windowsize
	 * @param windowsize
	 * @param offset 
	 */
	public WindowedLinearRegressionProcessor(int windowsize, int offset) {
		this.windowsize = windowsize;
		this.offset = offset;
		this.regdefined = false;
		
	}
	
	/**
	 * Use reg as the linear regression to predict. The {@link #process(DoubleTimeSeries)} function simply calls
	 * {@link LinearRegression#predict(Matrix)} with the times in the series as input
	 * @param reg
	 */
	public WindowedLinearRegressionProcessor(LinearRegression reg) {
		this.reg = reg;
		this.regdefined = true;
	}

	public WindowedLinearRegressionProcessor(DoubleTimeSeries yearFirstHalf,int i) {
		WindowedLinearRegressionProcessor inner = new WindowedLinearRegressionProcessor(i);
		inner.process(yearFirstHalf);
		this.reg = inner.reg;
		this.windowsize = i;
		this.regdefined = true;
	}
	
	public WindowedLinearRegressionProcessor(DoubleTimeSeries yearFirstHalf,int windowsize, int offset) {
		WindowedLinearRegressionProcessor inner = new WindowedLinearRegressionProcessor(windowsize,offset);
		inner.process(yearFirstHalf);
		this.reg = inner.reg;
		this.windowsize = windowsize;
		this.offset = offset;
		this.regdefined = true;
	}

	@Override
	public void process(DoubleTimeSeries series) {
		Matrix x = new Matrix(new double[][]{ArrayUtils.longToDouble(series.getTimes())}).transpose();
		List<IndependentPair<double[], double[]>> instances = new ArrayList<IndependentPair<double[], double[]>>();
		double[] data = series.getData();
		
		for (int i = this.windowsize + (offset - 1); i < series.size(); i++) {
			int start = i - this.windowsize - (offset - 1);
//			System.out.format("Range %d->%d (inclusive) used to calculate: %d\n",start,start+this.windowsize-1,i);
			double[] datawindow = new double[this.windowsize];
			System.arraycopy(data, start, datawindow, 0, this.windowsize);
			instances.add(IndependentPair.pair(datawindow, new double[]{data[i]}));
		}
		if(!regdefined)
		{
			this.reg = new LinearRegression();
			this.reg.estimate(instances);
		}
		System.out.println(this.reg);
		Iterator<IndependentPair<double[], double[]>> instanceIter = instances.iterator();
		for (int i = this.windowsize + (offset - 1); i < series.size(); i++) {
			data[i] = this.reg.predict(instanceIter.next().firstObject())[0];
		}
		long[] times = series.getTimes();
		long begin = times[this.windowsize + (offset - 1)];
		long end = times[times.length - 1];
		series.internalAssign(series.get(begin, end));
	}
	
	/**
	 * @param regdefined if true, process holds its last {@link LinearRegression}
	 */
	public void holdreg(boolean regdefined){
		this.regdefined = regdefined;
	}
	
	/**
	 * @return the {@link WindowedLinearRegressionProcessor}'s underlying {@link LinearRegression} model
	 */
	public LinearRegression getRegression() {
		return this.reg;
	}

}
